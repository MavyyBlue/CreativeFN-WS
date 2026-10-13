package dev.creativelogic.mobile.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.pm.ServiceInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation
import androidx.core.graphics.withScale
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.ScrollView
import android.widget.TextView
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.graph.Blueprint
import dev.creativelogic.graph.BlueprintGenerator
import dev.creativelogic.graph.MechanicGraphCodec
import dev.creativelogic.mobile.R
import dev.creativelogic.mobile.blueprint.BlueprintProgress
import dev.creativelogic.model.SavedMechanic
import kotlin.math.abs
import kotlin.math.min
import android.text.TextUtils

/** Explicitly user-launched reference window. Does not access Fortnite, draw continuously or run simulation. */
class BlueprintOverlayService : Service() {
    companion object {
        private const val CHANNEL = "blueprint_reference"
        private const val NOTIFICATION_ID = 47
        private const val ACTION_START = "dev.creativelogic.mobile.overlay.START"
        private const val ACTION_STOP = "dev.creativelogic.mobile.overlay.STOP"
        private const val PREFS = "blueprint-overlay"
        private const val DOCUMENT = "saved_document"
        fun launch(context: Context, mechanic: SavedMechanic, codec: MechanicGraphCodec) {
            require(Settings.canDrawOverlays(context)) { "Draw Over Other Apps permission is required" }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
                putString(DOCUMENT, codec.encode(mechanic))
                putString("selected", mechanic.mechanicMetadata.id)
            }
            context.startForegroundService(Intent(context, BlueprintOverlayService::class.java).setAction(ACTION_START))
        }
    }

    private val prefs by lazy { getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    private val manager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private var root: FrameLayout? = null
    private var params: WindowManager.LayoutParams? = null
    private var blueprint: Blueprint? = null
    private var expanded = false
    private var tab = 0
    private var step = 0
    private var tabsVisible = true
    private var resizeVisible = false
    private var warningsVisible = false
    private var panelWidthPercent = 52
    private var panelHeightPercent = 52

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val notifications = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notifications.createNotificationChannel(NotificationChannel(CHANNEL, "Active blueprint overlay", NotificationManager.IMPORTANCE_LOW))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }
        // FGS starts only as a consequence of a visible-app user gesture. No boot/autostart.
        val notification = notification()
        if (Build.VERSION.SDK_INT >= 34) startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        else startForeground(NOTIFICATION_ID, notification)
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return START_NOT_STICKY }
        val source = prefs.getString(DOCUMENT, null)
        val saved = try { source?.let { MechanicGraphCodec(InitialDeviceCatalog.catalog).decode(it) } }
            catch (_: Exception) { null }
        if (saved == null) { stopSelf(); return START_NOT_STICKY }
        blueprint = BlueprintGenerator.create(saved, InitialDeviceCatalog.catalog)
        step = prefs.getInt("step:${saved.mechanicMetadata.id}", 0).coerceIn(0, (blueprint!!.steps.size - 1).coerceAtLeast(0))
        tabsVisible = prefs.getBoolean("tabsVisible", true)
        if (root == null) showWindow(false) else render()
        return START_NOT_STICKY
    }

    private fun notification(): Notification {
        val stop = PendingIntent.getService(this, 1,
            Intent(this, BlueprintOverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("CreativeFN WS blueprint")
            .setContentText("Floating reference active • Tap FN to expand")
            .setOngoing(true).addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Overlay", stop)
            .build()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()
    private fun color(hex: String) = hex.toColorInt()
    private fun background(hex: String, radius: Int = 14): GradientDrawable = GradientDrawable().apply {
        setColor(color(hex)); cornerRadius = dp(radius).toFloat(); setStroke(dp(1), color("#515568"))
    }
    private fun bounds(): Pair<Int, Int> = if (Build.VERSION.SDK_INT >= 30) {
        manager.currentWindowMetrics.bounds.let { it.width() to it.height() }
    } else resources.displayMetrics.run { widthPixels to heightPixels }

    private fun orientationKey(): String {
        val (width, height) = bounds()
        return if (OverlayPanelSizing.isLandscape(width, height)) "landscape" else "portrait"
    }
    private fun positionKey(panel: Boolean) = "${if (panel) "panel" else "bubble"}-${orientationKey()}"

    private fun loadSizePreferences() {
        val (width, height) = bounds()
        val suffix = orientationKey()
        panelWidthPercent = prefs.getInt("widthPct:$suffix", OverlayPanelSizing.defaultWidthPercent(width, height))
            .coerceIn(40, 95)
        panelHeightPercent = prefs.getInt("heightPct:$suffix", OverlayPanelSizing.defaultHeightPercent(width, height))
            .coerceIn(32, 92)
    }
    private fun sizes(panel: Boolean): Pair<Int, Int> {
        if (!panel) return dp(58) to dp(58)
        val (width, height) = bounds()
        val size = OverlayPanelSizing.calculate(width, height, resources.displayMetrics.density,
            panelWidthPercent, panelHeightPercent)
        return size.width to size.height
    }
    private fun clamp(params: WindowManager.LayoutParams) {
        val (w, h) = bounds()
        val (x, y) = OverlayPanelSizing.clampPosition(params.x, params.y, params.width, params.height, w, h)
        params.x = x
        params.y = y
    }
    private fun showWindow(panel: Boolean) {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        expanded = panel
        if (panel) loadSizePreferences() else resizeVisible = false
        val (width, height) = sizes(panel)
        val prefix = positionKey(panel)
        val configuration = WindowManager.LayoutParams(width, height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt("${prefix}X", if (panel) dp(12) else dp(20))
            y = prefs.getInt("${prefix}Y", if (panel) dp(20) else dp(80))
            clamp(this)
        }
        try {
            val window = root ?: FrameLayout(this).also { root = it; manager.addView(it, configuration) }
            params = configuration
            if (window.isAttachedToWindow) manager.updateViewLayout(window, configuration)
            render()
        } catch (_: Exception) { stopSelf() }
    }

    /** On-demand only, from a touch gesture; persists its setting at gesture completion. */
    private fun resizePanel(widthPercent: Int, heightPercent: Int, save: Boolean) {
        if (!expanded) return
        panelWidthPercent = widthPercent.coerceIn(40, 95)
        panelHeightPercent = heightPercent.coerceIn(32, 92)
        val (w, h) = sizes(true)
        val p = params ?: return
        p.width = w
        p.height = h
        clamp(p)
        try { root?.let { manager.updateViewLayout(it, p) } }
        catch (_: Exception) { stopSelf(); return }
        if (save) prefs.edit {
            val orientation = orientationKey()
            putInt("widthPct:$orientation", panelWidthPercent)
            putInt("heightPct:$orientation", panelHeightPercent)
            putInt("${positionKey(true)}X", p.x)
            putInt("${positionKey(true)}Y", p.y)
        }
    }

    private fun moveTo(x: Int, y: Int, save: Boolean) {
        val p = params ?: return
        p.x = x; p.y = y; clamp(p)
        try { root?.let { manager.updateViewLayout(it, p) } } catch (_: Exception) { stopSelf(); return }
        if (save) {
            val prefix = positionKey(expanded)
            prefs.edit {
                putInt("${prefix}X", p.x)
                putInt("${prefix}Y", p.y)
            }
        }
    }

    private fun drag(control: View, click: () -> Unit) {
        control.setOnTouchListener(object : View.OnTouchListener {
            var downX = 0f; var downY = 0f; var startX = 0; var startY = 0; var moved = false
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = e.rawX; downY = e.rawY; startX = params?.x ?: 0; startY = params?.y ?: 0
                        moved = false; return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (abs(e.rawX - downX) > dp(7) || abs(e.rawY - downY) > dp(7)) moved = true
                        if (moved) moveTo(startX + (e.rawX - downX).toInt(), startY + (e.rawY - downY).toInt(), false)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (moved) moveTo(params?.x ?: startX, params?.y ?: startY, true)
                        else { v.performClick(); click() }
                        return true
                    }
                    MotionEvent.ACTION_CANCEL -> return true
                }
                return false
            }
        })
    }
    private fun label(text: String, size: Float = 14f, bold: Boolean = false): TextView = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(Color.WHITE); setPadding(dp(9), dp(7), dp(9), dp(7))
        if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
    }
    private fun button(text: String, action: () -> Unit): TextView = label(text, 12f).apply {
        gravity = Gravity.CENTER
        background = background("#303D57", 10)
        isClickable = true
        isFocusable = true
        minHeight = dp(32)
        setPadding(dp(8), dp(4), dp(8), dp(4))
        setOnClickListener { action() }
    }
    private fun heading(text: String) = label(text, 15f, true)

    private fun percentText(labelText: String, amount: Int): String = "$labelText $amount%"

    private fun slider(labelText: String, isWidthControl: Boolean): LinearLayout {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val current = if (isWidthControl) panelWidthPercent else panelHeightPercent
        val description = label(percentText(labelText, current), 12f).apply { minWidth = dp(84) }
        row.addView(description)
        val seek = SeekBar(this).apply {
            min = if (isWidthControl) 40 else 32
            max = if (isWidthControl) 95 else 92
            progress = current
            contentDescription = "$labelText of blueprint overlay"
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (!fromUser) return
                    description.text = percentText(labelText, progress)
                    resizePanel(if (isWidthControl) progress else panelWidthPercent,
                        if (isWidthControl) panelHeightPercent else progress, save = false)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    resizePanel(panelWidthPercent, panelHeightPercent, save = true)
                }
            })
        }
        row.addView(seek, LinearLayout.LayoutParams(0, dp(36), 1f))
        return row
    }

    private fun render() {
        val window = root ?: return
        window.removeAllViews()
        if (!expanded) {
            val bubble = label("FN", 19f, true).apply {
                background = background("#263E61", 30)
                gravity = Gravity.CENTER
                contentDescription = "Expand CreativeFN blueprint. Drag to move."
            }
            window.addView(bubble, FrameLayout.LayoutParams(-1, -1))
            drag(bubble) { showWindow(true) }
            return
        }
        val guide = blueprint ?: return
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = background("#181C2A") }
        window.addView(column, FrameLayout.LayoutParams(-1, -1))
        val toolbar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val handle = label("⋮⋮ ${guide.name}", 13f, true).apply {
            contentDescription = "Drag blueprint window"
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        toolbar.addView(handle, LinearLayout.LayoutParams(0, dp(38), 1f))
        toolbar.addView(button(if (tabsVisible) "Tabs −" else "Tabs +") {
            tabsVisible = !tabsVisible
            prefs.edit { putBoolean("tabsVisible", tabsVisible) }
            render()
        })
        toolbar.addView(button(if (resizeVisible) "Done" else "Size") {
            resizeVisible = !resizeVisible
            render()
        })
        toolbar.addView(button("−") { showWindow(false) }.apply { contentDescription = "Collapse blueprint to FN bubble" })
        toolbar.addView(button("Stop") { stopSelf() })
        column.addView(toolbar)
        drag(handle) { }

        if (resizeVisible) {
            column.addView(slider("Width", isWidthControl = true))
            column.addView(slider("Height", isWidthControl = false))
            column.addView(button("Reset to compact size") {
                val (w, h) = bounds()
                resizePanel(OverlayPanelSizing.defaultWidthPercent(w, h),
                    OverlayPanelSizing.defaultHeightPercent(w, h), save = true)
                resizeVisible = false
                render()
            })
        }
        if (tabsVisible) {
            val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            listOf("Steps", "Devices", "Links", "Graph").forEachIndexed { index, name ->
                val tabButton = button(if (tab == index) "• $name" else name) { tab = index; render() }
                tabButton.contentDescription = "Show $name blueprint tab"
                nav.addView(tabButton, LinearLayout.LayoutParams(0, dp(34), 1f))
            }
            column.addView(nav)
        }
        val scroller = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(6), dp(2), dp(6), dp(8))
        }
        scroller.addView(content)
        column.addView(scroller, LinearLayout.LayoutParams(-1, 0, 1f))
        when (tab) {
            0 -> showSteps(content, guide)
            1 -> showDevices(content, guide)
            2 -> showLinks(content, guide)
            else -> showGraph(content, guide)
        }
    }

    private fun showSteps(layout: LinearLayout, guide: Blueprint) {
        if (guide.steps.isEmpty()) { layout.addView(label("No construction steps. Review your saved mechanic.")); return }
        layout.addView(heading("Build checklist · ${step + 1}/${guide.steps.size}"))
        val progress = BlueprintProgress(this, guide.mechanicId)
        val checks = progress.checked()
        val check = CheckBox(this).apply {
            text = guide.steps[step]; setTextColor(Color.WHITE); textSize = 14f
            isChecked = step in checks
            setOnCheckedChangeListener { _, active ->
                val updated = progress.checked().toMutableSet()
                if (active) updated.add(step) else updated.remove(step)
                progress.save(updated)
            }
        }
        layout.addView(check)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(button("Previous") { step = (step - 1).coerceAtLeast(0); rememberStep(guide); render() }, LinearLayout.LayoutParams(0, dp(34), 1f))
        row.addView(button("Next") { step = (step + 1).coerceAtMost(guide.steps.lastIndex); rememberStep(guide); render() }, LinearLayout.LayoutParams(0, dp(34), 1f))
        layout.addView(row)
        layout.addView(heading("Devices required"))
        guide.quantities.forEach { (name, count) -> layout.addView(label("$count × $name", 12f)) }
        if (guide.warnings.isNotEmpty()) {
            layout.addView(button("${if (warningsVisible) "Hide" else "Show"} verification notes (${guide.warnings.size})") {
                warningsVisible = !warningsVisible
                render()
            })
            if (warningsVisible) guide.warnings.forEach { layout.addView(label("• $it", 12f)) }
        }
    }
    private fun rememberStep(guide: Blueprint) {
        prefs.edit { putInt("step:${guide.mechanicId}", step) }
    }
    private fun showDevices(layout: LinearLayout, guide: Blueprint) {
        layout.addView(heading("Device configuration"))
        guide.devices.forEach { device ->
            layout.addView(heading("${device.name} • ${device.type}"))
            device.options.forEach { layout.addView(label("${it.name}: ${it.value}")) }
            device.notes?.takeIf(String::isNotBlank)?.let { layout.addView(label("Builder note: $it")) }
        }
        layout.addView(label("Only settings modeled by this workshop are shown. Check Fortnite for other options."))
    }
    private fun showLinks(layout: LinearLayout, guide: Blueprint) {
        layout.addView(heading("Direct Event Bindings"))
        if (guide.bindings.isEmpty()) layout.addView(label("No bindings saved"))
        guide.bindings.forEachIndexed { index, connection ->
            layout.addView(label("${index + 1}. ${connection.sourceName} / ${connection.eventName}\n  → ${connection.targetName} / ${connection.functionName}"))
        }
    }
    private fun showGraph(layout: LinearLayout, guide: Blueprint) {
        val graph = BlueprintGraphView(this).apply { bind(guide) }
        // The graph occupies the entire available tab viewport. A tall offscreen canvas
        // made the previous small panel appear empty even when it held real nodes.
        val topControlsDp = 38 + (if (tabsVisible) 34 else 0) + (if (resizeVisible) 104 else 0)
        val graphHeight = ((params?.height ?: dp(260)) - dp(topControlsDp + 14)).coerceAtLeast(dp(88))
        val frame = FrameLayout(this)
        frame.addView(graph, FrameLayout.LayoutParams(-1, -1))
        frame.addView(button("Fit") { graph.fitToContent() }.apply {
            contentDescription = "Fit all saved devices and bindings into the blueprint graph"
        }, FrameLayout.LayoutParams(dp(58), dp(32), Gravity.TOP or Gravity.END).apply {
            topMargin = dp(4)
            rightMargin = dp(4)
        })
        layout.addView(frame, LinearLayout.LayoutParams(-1, graphHeight))
        layout.addView(label("${guide.devices.size} devices · ${guide.bindings.size} links · pinch/drag to explore", 12f))
        if (guide.devices.isEmpty()) layout.addView(label("Add devices to the saved mechanic to see its graph."))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Refit only the overlay window: state and checked steps are retained.
        if (root != null) showWindow(expanded)
    }
    override fun onDestroy() {
        root?.let { try { manager.removeViewImmediate(it) } catch (_: Exception) { } }
        root = null; params = null
        super.onDestroy()
    }
}

internal class BlueprintGraphView(context: Context) : View(context) {
    private lateinit var blueprint: Blueprint
    private var nodes: Map<String, dev.creativelogic.graph.BlueprintDevice> = emptyMap()
    private val nodeBounds = RectF() // never allocate objects during onDraw
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var zoom = 1f
    private var panX = 0f
    private var panY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private val scaler = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // Zoom toward the gesture focus, not an arbitrary screen corner.
            val old = zoom
            zoom = (zoom * detector.scaleFactor).coerceIn(0.02f, 4.5f)
            val ratio = zoom / old
            panX = detector.focusX - (detector.focusX - panX) * ratio
            panY = detector.focusY - (detector.focusY - panY) * ratio
            invalidate()
            return true
        }
    })

    fun bind(value: Blueprint) {
        blueprint = value
        nodes = value.devices.associateBy { it.id }
        if (width > 0 && height > 0) fitToContent() else invalidate()
    }

    init {
        setBackgroundColor(Color.rgb(20, 28, 43))
        contentDescription = "Blueprint graph, pinch to zoom and drag to pan"
    }

    fun fitToContent() {
        if (!::blueprint.isInitialized || width <= 0 || height <= 0) return
        if (blueprint.devices.isEmpty()) {
            zoom = 1f; panX = 0f; panY = 0f; invalidate(); return
        }
        val left = blueprint.devices.minOf { it.graphX }
        val top = blueprint.devices.minOf { it.graphY }
        val right = blueprint.devices.maxOf { it.graphX } + 176f
        val bottom = blueprint.devices.maxOf { it.graphY } + 66f
        val graphWidth = (right - left).coerceAtLeast(1f)
        val graphHeight = (bottom - top).coerceAtLeast(1f)
        val margin = resources.displayMetrics.density * 13f
        // Pixel-space zoom: source graph coordinates are logical scene units, not density-scaled dp.
        zoom = min(((width - margin * 2f).coerceAtLeast(1f)) / graphWidth,
            ((height - margin * 2f).coerceAtLeast(1f)) / graphHeight).coerceIn(0.02f, 4.5f)
        panX = (width - graphWidth * zoom) / 2f - left * zoom
        panY = (height - graphHeight * zoom) / 2f - top * zoom
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fitToContent()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!::blueprint.isInitialized) return
        if (blueprint.devices.isEmpty()) {
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            paint.textSize = 15f * resources.displayMetrics.density
            canvas.drawText("No graph devices", 14f, 30f * resources.displayMetrics.density, paint)
            return
        }
        canvas.withTranslation(panX, panY) {
            withScale(zoom, zoom) {
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(110, 218, 246)
                paint.strokeWidth = 2.4f
                blueprint.bindings.forEach { link ->
                    val a = nodes[link.sourceId]; val b = nodes[link.targetId]
                    if (a != null && b != null) drawLine(a.graphX + 176f, a.graphY + 33f,
                        b.graphX, b.graphY + 33f, paint)
                }
                blueprint.devices.forEach { device ->
                    nodeBounds.set(device.graphX, device.graphY, device.graphX + 176f, device.graphY + 66f)
                    paint.style = Paint.Style.FILL
                    paint.color = Color.rgb(53, 70, 107)
                    drawRoundRect(nodeBounds, 10f, 10f, paint)
                    paint.style = Paint.Style.STROKE
                    paint.color = Color.rgb(123, 168, 237)
                    paint.strokeWidth = 1.7f
                    drawRoundRect(nodeBounds, 10f, 10f, paint)
                    paint.style = Paint.Style.FILL
                    paint.color = Color.WHITE
                    paint.textSize = 15f
                    drawText(device.name, 0, min(device.name.length, 20),
                        device.graphX + 9f, device.graphY + 26f, paint)
                    paint.textSize = 12f
                    paint.color = Color.rgb(205, 221, 247)
                    drawText(device.type, 0, min(device.type.length, 24),
                        device.graphX + 9f, device.graphY + 48f, paint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaler.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                lastX = event.x; lastY = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1 && !scaler.isInProgress) {
                    panX += event.x - lastX
                    panY += event.y - lastY
                    invalidate()
                }
                lastX = event.x; lastY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val index = if (event.actionIndex == 0 && event.pointerCount > 1) 1 else 0
                lastX = event.getX(index)
                lastY = event.getY(index)
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean { super.performClick(); return true }
}

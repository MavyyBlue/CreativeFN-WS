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
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
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
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(DOCUMENT, codec.encode(mechanic))
                .putString("selected", mechanic.mechanicMetadata.id).apply()
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
    private fun color(hex: String) = Color.parseColor(hex)
    private fun background(hex: String, radius: Int = 14): GradientDrawable = GradientDrawable().apply {
        setColor(color(hex)); cornerRadius = dp(radius).toFloat(); setStroke(dp(1), color("#515568"))
    }
    private fun bounds(): Pair<Int, Int> = if (Build.VERSION.SDK_INT >= 30) {
        manager.currentWindowMetrics.bounds.let { it.width() to it.height() }
    } else resources.displayMetrics.run { widthPixels to heightPixels }
    private fun sizes(panel: Boolean): Pair<Int, Int> {
        val (width, height) = bounds()
        return if (!panel) dp(58) to dp(58) else
            min(width - dp(12), dp(660)) to min(height - dp(48), dp(570))
    }
    private fun clamp(params: WindowManager.LayoutParams) {
        val (w, h) = bounds()
        params.x = params.x.coerceIn(0, (w - params.width).coerceAtLeast(0))
        params.y = params.y.coerceIn(0, (h - params.height).coerceAtLeast(0))
    }
    private fun showWindow(panel: Boolean) {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        expanded = panel
        val (width, height) = sizes(panel)
        val prefix = if (panel) "panel" else "bubble"
        val configuration = WindowManager.LayoutParams(width, height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START
            x = prefs.getInt("${prefix}X", if (panel) dp(16) else dp(20))
            y = prefs.getInt("${prefix}Y", if (panel) dp(42) else dp(120))
            clamp(this)
        }
        try {
            val window = root ?: FrameLayout(this).also { root = it; manager.addView(it, configuration) }
            params = configuration
            if (root === window && window.isAttachedToWindow) manager.updateViewLayout(window, configuration)
            render()
        } catch (_: Exception) { stopSelf() }
    }

    private fun moveTo(x: Int, y: Int, save: Boolean) {
        val p = params ?: return
        p.x = x; p.y = y; clamp(p)
        try { root?.let { manager.updateViewLayout(it, p) } } catch (_: Exception) { stopSelf(); return }
        if (save) {
            val prefix = if (expanded) "panel" else "bubble"
            prefs.edit().putInt("${prefix}X", p.x).putInt("${prefix}Y", p.y).apply()
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
    private fun button(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text; textSize = 12f; isAllCaps = false
        setOnClickListener { action() }
    }
    private fun heading(text: String) = label(text, 16f, true)

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
        val handle = label("⋮⋮  ${guide.name.take(25)}", 14f, true).apply { contentDescription = "Drag blueprint window" }
        toolbar.addView(handle, LinearLayout.LayoutParams(0, dp(52), 1f))
        toolbar.addView(button("−") { showWindow(false) })
        toolbar.addView(button("Stop") { stopSelf() })
        column.addView(toolbar)
        drag(handle) { }
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("Steps", "Devices", "Links", "Graph").forEachIndexed { index, name ->
            nav.addView(button(if (tab == index) "• $name" else name) { tab = index; render() },
                LinearLayout.LayoutParams(0, dp(45), 1f))
        }
        column.addView(nav)
        val scroller = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(5), dp(2), dp(5), dp(10)) }
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
        layout.addView(heading("Build checklist • ${step + 1}/${guide.steps.size}"))
        val progress = BlueprintProgress(this, guide.mechanicId)
        val checks = progress.checked()
        val check = CheckBox(this).apply {
            text = guide.steps[step]; setTextColor(Color.WHITE); textSize = 15f
            isChecked = step in checks
            setOnCheckedChangeListener { _, active ->
                val updated = progress.checked().toMutableSet()
                if (active) updated.add(step) else updated.remove(step)
                progress.save(updated)
            }
        }
        layout.addView(check)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(button("Previous") { step = (step - 1).coerceAtLeast(0); rememberStep(guide); render() })
        row.addView(button("Next") { step = (step + 1).coerceAtMost(guide.steps.lastIndex); rememberStep(guide); render() })
        layout.addView(row)
        layout.addView(heading("Devices required"))
        guide.quantities.forEach { (name, count) -> layout.addView(label("$count × $name")) }
        if (guide.warnings.isNotEmpty()) {
            layout.addView(heading("Limits and manual checks"))
            guide.warnings.forEach { layout.addView(label("• $it", 12f)) }
        }
    }
    private fun rememberStep(guide: Blueprint) {
        prefs.edit().putInt("step:${guide.mechanicId}", step).apply()
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
        layout.addView(heading("Device graph • pinch to zoom, drag to pan"))
        val graph = BlueprintGraphView(this, guide)
        layout.addView(graph, LinearLayout.LayoutParams(-1, dp(350)))
        layout.addView(label("Graph is a reference. Follow the Steps and Devices tabs for exact saved settings."))
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

private class BlueprintGraphView(context: Context, private val blueprint: Blueprint) : View(context) {
    private val density = resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var zoom = 0.8f
    private var panX = 15f
    private var panY = 15f
    private var lastX = 0f
    private var lastY = 0f
    private val scaler = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            zoom = (zoom * detector.scaleFactor).coerceIn(0.2f, 2.5f)
            invalidate(); return true
        }
    })
    init { setBackgroundColor(Color.rgb(20, 28, 43)); contentDescription = "Blueprint graph, pinch to zoom and drag to pan" }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (blueprint.devices.isNotEmpty() && w > 0 && h > 0) {
            val left = blueprint.devices.minOf { it.graphX }
            val top = blueprint.devices.minOf { it.graphY }
            val right = blueprint.devices.maxOf { it.graphX } + 176f
            val bottom = blueprint.devices.maxOf { it.graphY } + 66f
            zoom = min(((w / density) - 32f) / (right - left).coerceAtLeast(1f),
                ((h / density) - 32f) / (bottom - top).coerceAtLeast(1f)).coerceIn(0.2f, 1.5f)
            panX = 16f * density - left * zoom * density
            panY = 16f * density - top * zoom * density
        }
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save(); canvas.translate(panX, panY); canvas.scale(zoom * density, zoom * density)
        val nodes = blueprint.devices.associateBy { it.id }
        paint.style = Paint.Style.STROKE; paint.color = Color.rgb(110, 218, 246); paint.strokeWidth = 2f
        blueprint.bindings.forEach { link ->
            val a = nodes[link.sourceId]; val b = nodes[link.targetId]
            if (a != null && b != null) canvas.drawLine(a.graphX + 176f, a.graphY + 32f, b.graphX, b.graphY + 32f, paint)
        }
        blueprint.devices.forEach { device ->
            paint.style = Paint.Style.FILL; paint.color = Color.rgb(53, 61, 94)
            canvas.drawRoundRect(RectF(device.graphX, device.graphY, device.graphX + 176f, device.graphY + 66f), 12f, 12f, paint)
            paint.color = Color.WHITE; paint.textSize = 15f
            canvas.drawText(device.name.take(20), device.graphX + 9f, device.graphY + 27f, paint)
            paint.textSize = 12f; paint.color = Color.rgb(190, 210, 234)
            canvas.drawText(device.type.take(24), device.graphX + 9f, device.graphY + 48f, paint)
        }
        canvas.restore()
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaler.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                lastX = event.x; lastY = event.y; return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1 && !scaler.isInProgress) {
                    panX += event.x - lastX; panY += event.y - lastY
                    invalidate()
                }
                lastX = event.x; lastY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                performClick(); return true
            }
            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
            MotionEvent.ACTION_POINTER_UP -> { lastX = event.getX(0); lastY = event.getY(0); return true }
        }
        return true
    }
    override fun performClick(): Boolean { super.performClick(); return true }
}

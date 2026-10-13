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
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.ScrollView
import android.widget.TextView
import android.text.TextWatcher
import android.text.Editable
import android.view.inputmethod.InputMethodManager
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.graph.Blueprint
import dev.creativelogic.graph.BlueprintGenerator
import dev.creativelogic.graph.BlueprintConnections
import dev.creativelogic.graph.MechanicGraphCodec
import dev.creativelogic.mobile.R
import dev.creativelogic.mobile.CreativeLogicApplication
import dev.creativelogic.model.BlueprintFolderV1
import dev.creativelogic.mobile.blueprint.BlueprintProgress
import dev.creativelogic.model.SavedMechanic
import kotlin.math.abs
import kotlin.math.min
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
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
    // Separate, tightly bounded TYPE_APPLICATION_OVERLAY: no invisible full-screen touch surface.
    private var navigatorRoot: FrameLayout? = null
    private var navigatorParams: WindowManager.LayoutParams? = null
    private var navigatorRenderedTab = -1
    private var navigatorUnavailable = false
    private val navigatorHeightDp = 56
    private val navigatorGapDp = 4
    private var menuVisible = false
    private var blueprint: Blueprint? = null
    private var expanded = false
    private var tab = 0
    private var step = 0
    private var tabsVisible = true
    private var resizeVisible = false
    private var warningsVisible = false
    private var panelWidthPercent = 52
    private var panelHeightPercent = 52
    private var savedDocument: SavedMechanic? = null
    private val work = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var linkSelectedId: String? = null
    private var selectedGraphId: String? = null
    private var focusedGraphId: String? = null
    private var expandedFolderId: String? = null
    private var pendingFolderRootId: String? = null
    private var editingFolderId: String? = null
    private var pendingFolderName = ""
    private var pendingFolderMembers: Set<String> = emptySet()
    private var folderSaving = false
    private var folderStatus: String? = null
    private var currentScroller: ScrollView? = null
    private var lastRenderedTab = 2
    private var linksScrollY = 0

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
        savedDocument = saved
        blueprint = BlueprintGenerator.create(saved, InitialDeviceCatalog.catalog)
        // Three stable destinations: Steps, Links directory, interactive Graph.
        tab = prefs.getInt("tab:${saved.mechanicMetadata.id}", 2).coerceIn(0, 2)
        linkSelectedId = null
        selectedGraphId = null
        focusedGraphId = null
        expandedFolderId = null
        pendingFolderRootId = null
        editingFolderId = null
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

    private fun maxHeightPercent(): Int {
        if (!tabsVisible) return 92
        val height = bounds().second
        val reserved = dp(navigatorHeightDp + navigatorGapDp + 10)
        return (((height - reserved).coerceAtLeast(1) * 100f) / height.coerceAtLeast(1))
            .toInt().coerceIn(32, 92)
    }

    private fun loadSizePreferences() {
        val (width, height) = bounds()
        val suffix = orientationKey()
        panelWidthPercent = prefs.getInt("widthPct:$suffix", OverlayPanelSizing.defaultWidthPercent(width, height))
            .coerceIn(40, 95)
        panelHeightPercent = prefs.getInt("heightPct:$suffix", OverlayPanelSizing.defaultHeightPercent(width, height))
            .coerceIn(32, maxHeightPercent())
    }
    private fun sizes(panel: Boolean): Pair<Int, Int> {
        if (!panel) return dp(58) to dp(58)
        val (width, height) = bounds()
        val size = OverlayPanelSizing.calculate(width, height, resources.displayMetrics.density,
            panelWidthPercent, panelHeightPercent)
        val reserve = if (tabsVisible) dp(navigatorHeightDp + navigatorGapDp + 10) else dp(16)
        return size.width to size.height.coerceAtMost((height - reserve).coerceAtLeast(dp(100)))
    }
    private fun clamp(params: WindowManager.LayoutParams) {
        val (w, h) = bounds()
        val (x, y) = if (expanded && tabsVisible) OverlayPanelSizing.clampPanelPosition(
            params.x, params.y, params.width, params.height, w, h,
            dp(navigatorHeightDp), dp(navigatorGapDp),
        ) else OverlayPanelSizing.clampPosition(params.x, params.y, params.width, params.height, w, h)
        params.x = x
        params.y = y
    }
    private fun showWindow(panel: Boolean) {
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        if (!panel) {
            closeFolderEditor()
            dismissNavigator()
            menuVisible = false
        }
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
        panelHeightPercent = heightPercent.coerceIn(32, maxHeightPercent())
        val (w, h) = sizes(true)
        val p = params ?: return
        p.width = w
        p.height = h
        clamp(p)
        try { root?.let { manager.updateViewLayout(it, p) }; syncNavigator() }
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
        try { root?.let { manager.updateViewLayout(it, p) }; syncNavigator() } catch (_: Exception) { stopSelf(); return }
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
            max = if (isWidthControl) 95 else maxHeightPercent()
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

    private fun switchTab(index: Int) {
        val next = CoverFlowModel.selectedTab(index)
        if (tab == next) return
        tab = next
        savedDocument?.mechanicMetadata?.id?.let { id -> prefs.edit { putInt("tab:$id", next) } }
        menuVisible = false
        resizeVisible = false
        render()
    }

    private fun dismissNavigator() {
        navigatorRoot?.let { try { manager.removeViewImmediate(it) } catch (_: Exception) { } }
        navigatorRoot = null
        navigatorParams = null
        navigatorRenderedTab = -1
    }

    /** The navigation owns only its small visible touch footprint, entirely outside the panel. */
    private fun syncNavigator() {
        if (!expanded || !tabsVisible || root == null || navigatorUnavailable) { dismissNavigator(); return }
        val panel = params ?: return
        val (screenWidth, screenHeight) = bounds()
        val navWidth = min(panel.width, dp(234)).coerceAtLeast(1)
        val navHeight = dp(navigatorHeightDp)
        val (x, y) = OverlayPanelSizing.navigatorPosition(
            panel.x, panel.y, panel.width, panel.height, navWidth, navHeight,
            dp(navigatorGapDp), screenWidth, screenHeight,
        )
        try {
            if (navigatorRoot == null) {
                val configuration = WindowManager.LayoutParams(navWidth, navHeight,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT).apply {
                    gravity = Gravity.TOP or Gravity.START
                    this.x = x
                    this.y = y
                }
                val view = FrameLayout(this).apply { background = null; isClickable = true }
                manager.addView(view, configuration)
                navigatorRoot = view
                navigatorParams = configuration
            } else {
                navigatorParams?.let { config ->
                    config.width = navWidth; config.height = navHeight
                    config.x = x; config.y = y
                    navigatorRoot?.let { manager.updateViewLayout(it, config) }
                }
            }
            if (navigatorRenderedTab != tab) renderNavigator()
        } catch (_: Exception) {
            // Some Android/game configurations may reject a second overlay window.
            // Keep the working blueprint panel and fall back to menu-based navigation.
            navigatorUnavailable = true
            dismissNavigator()
        }
    }

    private fun renderNavigator() {
        val navigation = navigatorRoot ?: return
        val previous = navigatorRenderedTab
        navigation.removeAllViews()
        navigatorRenderedTab = tab
        val swatch = listOf("#325276", "#38446B", "#315E79")
        val symbols = listOf("☑", "↗", "◇")
        CoverFlowModel.cardOrder(tab).forEach { index ->
            val centered = index == tab
            val side = CoverFlowModel.cardSide(tab, index)
            val startSide = if (previous >= 0) CoverFlowModel.cardSide(previous, index) else side
            val tile = label("${symbols[index]}\n${CoverFlowModel.titles[index]}", if (centered) 13f else 12f, centered).apply {
                gravity = Gravity.CENTER
                background = background(swatch[index], 11)
                contentDescription = "${CoverFlowModel.titles[index]} blueprint view${if (centered) ", selected" else ""}"
                elevation = dp(if (centered) 7 else 2).toFloat()
                cameraDistance = dp(700).toFloat()
                rotationY = if (startSide == 0) 0f else if (startSide < 0) 40f else -40f
                scaleX = if (startSide == 0) 1f else 0.82f
                scaleY = if (startSide == 0) 1f else 0.82f
                translationX = dp(64 * startSide).toFloat()
                alpha = if (startSide == 0) 1f else 0.83f
                // A card remains clickable through TalkBack as well as taps and flings.
                setOnClickListener { switchTab(index) }
                var touchX = 0f
                setOnTouchListener { v, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> { touchX = event.rawX; true }
                        MotionEvent.ACTION_MOVE -> true
                        MotionEvent.ACTION_CANCEL -> true
                        MotionEvent.ACTION_UP -> {
                            val delta = event.rawX - touchX
                            if (abs(delta) >= dp(30)) switchTab(CoverFlowModel.swipe(tab, delta, dp(30).toFloat()))
                            else v.performClick()
                            true
                        }
                        else -> false
                    }
                }
            }
            navigation.addView(tile, FrameLayout.LayoutParams(dp(82), dp(46), Gravity.CENTER))
            if (previous >= 0 && previous != tab) {
                // Short, gesture-triggered transition; no idle animator or background loop.
                tile.animate().translationX(dp(64 * side).toFloat())
                    .rotationY(if (side == 0) 0f else if (side < 0) 40f else -40f)
                    .scaleX(if (centered) 1f else 0.82f)
                    .scaleY(if (centered) 1f else 0.82f)
                    .alpha(if (centered) 1f else 0.83f)
                    .setDuration(170L).start()
            }
        }
    }

    private fun render() {
        if (tab != 1 && (pendingFolderRootId != null || editingFolderId != null)) closeFolderEditor()
        val window = root ?: return
        if (lastRenderedTab == 1 && currentScroller != null) linksScrollY = currentScroller?.scrollY ?: linksScrollY
        currentScroller = null
        lastRenderedTab = tab
        window.removeAllViews()
        if (!expanded) {
            dismissNavigator()
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
        val surface = FrameLayout(this).apply {
            background = background("#181C2A")
            clipToOutline = true
        }
        window.addView(surface, FrameLayout.LayoutParams(-1, -1))
        if (tab == 2) {
            showGraph(surface, guide)
        } else {
            val scroller = ScrollView(this).apply { isFillViewport = true }
            val content = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                // Minimal inset for the small floating drag/menu controls; otherwise full width.
                setPadding(dp(10), dp(37), dp(10), dp(8))
            }
            scroller.addView(content)
            surface.addView(scroller, FrameLayout.LayoutParams(-1, -1))
            currentScroller = scroller
            if (tab == 1 && linksScrollY > 0) scroller.post { scroller.scrollTo(0, linksScrollY) }
            if (tab == 0) showSteps(content, guide) else showLinks(content, guide)
        }

        // Controls hover in the panel corners; they do not consume a whole toolbar row.
        val handle = button("⋮⋮") { }.apply { contentDescription = "Drag floating blueprint" }
        surface.addView(handle, FrameLayout.LayoutParams(dp(34), dp(30), Gravity.TOP or Gravity.START).apply {
            leftMargin = dp(4); topMargin = dp(4)
        })
        drag(handle) { }
        surface.addView(button(if (menuVisible) "×" else "⋯") {
            menuVisible = !menuVisible
            resizeVisible = false
            render()
        }.apply { contentDescription = "Blueprint window controls" },
            FrameLayout.LayoutParams(dp(35), dp(30), Gravity.TOP or Gravity.END).apply {
                rightMargin = dp(4); topMargin = dp(4)
            })
        if (menuVisible) renderControls(surface)
        if (resizeVisible) renderSizeControls(surface)
        syncNavigator()
    }

    private fun renderControls(surface: FrameLayout) {
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = background("#252F47", 11)
            setPadding(dp(5), dp(5), dp(5), dp(5))
        }
        if (navigatorUnavailable || !tabsVisible) {
            CoverFlowModel.titles.forEachIndexed { index, title ->
                column.addView(button("View $title") { switchTab(index) })
            }
        }
        column.addView(button("Size") {
            menuVisible = false
            resizeVisible = true
            render()
        })
        column.addView(button(if (tabsVisible) "Hide navigation" else "Show navigation") {
            tabsVisible = !tabsVisible
            prefs.edit { putBoolean("tabsVisible", tabsVisible) }
            menuVisible = false
            panelHeightPercent = panelHeightPercent.coerceAtMost(maxHeightPercent())
            // Panel bounds change when the separate navigator is visible.
            val p = params
            if (p != null) {
                val (w, h) = sizes(true); p.width = w; p.height = h; clamp(p)
                try { root?.let { manager.updateViewLayout(it, p) } } catch (_: Exception) { stopSelf() }
            }
            render()
        })
        column.addView(button("Collapse to FN") { showWindow(false) })
        column.addView(button("Stop overlay") { stopSelf() })
        val scroll = ScrollView(this).apply { background = null; addView(column) }
        val menuHeight = min(dp(172), ((params?.height ?: dp(250)) - dp(42)).coerceAtLeast(dp(46)))
        surface.addView(scroll, FrameLayout.LayoutParams(dp(160), menuHeight, Gravity.TOP or Gravity.END).apply {
            topMargin = dp(36); rightMargin = dp(4)
        })
    }

    private fun renderSizeControls(surface: FrameLayout) {
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = background("#27334F") }
        column.addView(slider("Width", isWidthControl = true))
        column.addView(slider("Height", isWidthControl = false))
        column.addView(button("Reset compact") {
            val (w, h) = bounds()
            resizePanel(OverlayPanelSizing.defaultWidthPercent(w, h),
                OverlayPanelSizing.defaultHeightPercent(w, h), save = true)
            resizeVisible = false
            render()
        })
        column.addView(button("Done") { resizeVisible = false; render() })
        val scroll = ScrollView(this).apply { addView(column) }
        val maxHeight = min(dp(168), ((params?.height ?: dp(250)) - dp(34)).coerceAtLeast(dp(46)))
        surface.addView(scroll, FrameLayout.LayoutParams(-1, maxHeight, Gravity.BOTTOM))
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
    /** Organizes the saved devices without adding visual noise to the Graph tab. */
    private fun showLinks(layout: LinearLayout, guide: Blueprint) {
        layout.addView(heading("Connections · ${guide.devices.size} devices / ${guide.bindings.size} bindings"))
        layout.addView(label("Choose a device to see what it sends and receives. Folders are only for organization.", 12f))
        folderStatus?.let { layout.addView(label(it, 12f)) }
        if (pendingFolderRootId != null || editingFolderId != null) showFolderEditor(layout, guide)
        guide.folders.forEach { folder ->
            val expanded = expandedFolderId == folder.id
            layout.addView(button("${if (expanded) "▾" else "▸"} ${folder.name}  (${folder.deviceIds.size})") {
                expandedFolderId = if (expanded) null else folder.id
                linkSelectedId = null
                render()
            })
            if (expanded) {
                val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                controls.addView(button("Rename") {
                    editingFolderId = folder.id
                    pendingFolderRootId = null
                    pendingFolderMembers = folder.deviceIds.toSet()
                    pendingFolderName = folder.name
                    render()
                }, LinearLayout.LayoutParams(0, dp(35), 1f))
                controls.addView(button("Ungroup") {
                    saveFolders(guide.folders.filterNot { it.id == folder.id })
                }, LinearLayout.LayoutParams(0, dp(35), 1f))
                layout.addView(controls)
                folder.deviceIds.forEach { id ->
                    guide.devices.firstOrNull { it.id == id }?.let { showLinkDevice(layout, guide, it, "  ") }
                }
            }
        }
        val assigned = guide.folders.flatMap { it.deviceIds }.toSet()
        val ungrouped = guide.devices.filterNot { it.id in assigned }
        if (ungrouped.isNotEmpty()) layout.addView(heading("Ungrouped devices"))
        ungrouped.forEach { showLinkDevice(layout, guide, it, "") }
        if (guide.devices.isEmpty()) layout.addView(label("No devices saved yet."))
    }

    private fun showLinkDevice(layout: LinearLayout, guide: Blueprint,
        device: dev.creativelogic.graph.BlueprintDevice, indent: String) {
        val count = BlueprintConnections.touching(guide, device.id).size
        layout.addView(button("$indent${if (linkSelectedId == device.id) "▾" else "▸"} ${device.name} · $count links") {
            linkSelectedId = if (linkSelectedId == device.id) null else device.id
            render()
        })
        if (linkSelectedId != device.id) return
        layout.addView(label("${device.type} · ${device.name}", 13f, true))
        val outgoing = guide.bindings.filter { it.sourceId == device.id }
        val incoming = guide.bindings.filter { it.targetId == device.id }
        layout.addView(label("Sends (${outgoing.size})", 12f, true))
        if (outgoing.isEmpty()) layout.addView(label("No outgoing bindings", 12f))
        outgoing.forEach { link -> layout.addView(label("${link.eventName} → ${link.targetName} / ${link.functionName}", 12f)) }
        layout.addView(label("Receives (${incoming.size})", 12f, true))
        if (incoming.isEmpty()) layout.addView(label("No incoming bindings", 12f))
        incoming.forEach { link -> layout.addView(label("${link.sourceName} / ${link.eventName} → ${link.functionName}", 12f)) }
        layout.addView(button("View in Graph") {
            selectedGraphId = device.id
            tab = 2
            render()
        })
        val assigned = guide.folders.flatMap { it.deviceIds }.toSet()
        if (device.id !in assigned) layout.addView(button("Group direct connections") {
            pendingFolderRootId = device.id
            editingFolderId = null
            pendingFolderName = ""
            pendingFolderMembers = BlueprintConnections.directlyConnected(guide, device.id) - assigned
            folderStatus = null
            render()
        })
    }

    private fun showFolderEditor(layout: LinearLayout, guide: Blueprint) {
        val editingId = editingFolderId
        val rootId = pendingFolderRootId
        val root = guide.devices.firstOrNull { it.id == rootId }
        layout.addView(heading(if (editingId != null) "Rename device folder" else "Create device folder"))
        if (root != null) {
            layout.addView(label("Direct connections of ${root.name}. Select which devices to include:", 12f))
            val assigned = guide.folders.flatMap { it.deviceIds }.toSet()
            val choices = BlueprintConnections.directlyConnected(guide, root.id) - assigned
            choices.forEach { id ->
                val device = guide.devices.firstOrNull { it.id == id } ?: return@forEach
                val checkbox = CheckBox(this).apply {
                    text = device.name
                    textSize = 13f
                    setTextColor(Color.WHITE)
                    isChecked = id in pendingFolderMembers
                    isEnabled = id != root.id && !folderSaving
                    setOnCheckedChangeListener { _, checked ->
                        pendingFolderMembers = if (checked) pendingFolderMembers + id else pendingFolderMembers - id
                    }
                }
                layout.addView(checkbox)
            }
        }
        val nameInput = EditText(this).apply {
            setSingleLine(true)
            hint = "Folder name"
            textSize = 14f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setText(pendingFolderName)
            isEnabled = !folderSaving
            filters = arrayOf(android.text.InputFilter.LengthFilter(60))
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    pendingFolderName = s?.toString().orEmpty()
                }
                override fun afterTextChanged(s: Editable?) = Unit
            })
            setOnClickListener { changeKeyboardFocus(true); requestFocus();
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(this, InputMethodManager.SHOW_IMPLICIT) }
        }
        layout.addView(nameInput)
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(button("Cancel") {
            closeFolderEditor()
            render()
        }, LinearLayout.LayoutParams(0, dp(35), 1f))
        actions.addView(button(if (folderSaving) "Saving…" else "Save folder") {
            if (!folderSaving) {
                val name = pendingFolderName.trim()
                if (name.isEmpty()) { folderStatus = "Enter a folder name first."; render() }
                else if (editingId != null) saveFolders(guide.folders.map { if (it.id == editingId) it.copy(name = name) else it })
                else if (rootId != null) {
                    val members = pendingFolderMembers.ifEmpty { setOf(rootId) }.toList()
                    saveFolders(guide.folders + BlueprintFolderV1(UUID.randomUUID().toString(), name, members))
                }
            }
        }, LinearLayout.LayoutParams(0, dp(35), 1f))
        layout.addView(actions)
    }

    private fun changeKeyboardFocus(allow: Boolean) {
        val config = params ?: return
        config.flags = if (allow) config.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            else config.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        try { root?.let { manager.updateViewLayout(it, config) } } catch (_: Exception) { stopSelf() }
        if (!allow) {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(root?.windowToken, 0)
            root?.clearFocus()
        }
    }
    private fun closeFolderEditor() {
        changeKeyboardFocus(false)
        pendingFolderRootId = null
        editingFolderId = null
        pendingFolderMembers = emptySet()
        pendingFolderName = ""
    }

    private fun saveFolders(folders: List<BlueprintFolderV1>) {
        if (folderSaving) return
        val id = savedDocument?.mechanicMetadata?.id ?: return
        // Return focus to Fortnite as soon as the editor is submitted.
        changeKeyboardFocus(false)
        folderSaving = true
        folderStatus = "Saving folder…"
        render()
        work.launch {
            try {
                val repository = (application as CreativeLogicApplication).repository
                val latest = repository.observeAll().first().firstOrNull { it.mechanicMetadata.id == id }
                    ?: error("Mechanic no longer exists in Library")
                val edited = latest.copy(folders = folders,
                    mechanicMetadata = latest.mechanicMetadata.copy(modifiedAtEpochMillis = System.currentTimeMillis()))
                val codec = MechanicGraphCodec(InitialDeviceCatalog.catalog)
                val encoded = codec.encode(edited) // validates folder IDs and preserved graph before writing
                repository.save(edited)
                savedDocument = edited
                prefs.edit { putString(DOCUMENT, encoded) }
                blueprint = BlueprintGenerator.create(edited, InitialDeviceCatalog.catalog)
                expandedFolderId = editingFolderId ?: folders.lastOrNull()?.id
                closeFolderEditor()
                folderStatus = "Folder saved in Library and .c1logic data."
            } catch (ex: Exception) {
                folderStatus = "Folder not saved: ${ex.localizedMessage ?: "check your Library"}"
            } finally {
                folderSaving = false
                render()
            }
        }
    }

    /** Full-bleed graph: changing inspectors never changes the graph viewport or node positions. */
    private fun showGraph(surface: FrameLayout, guide: Blueprint) {
        val graph = BlueprintGraphView(this).apply {
            bind(guide)
            setFocus(focusedGraphId)
        }
        surface.addView(graph, FrameLayout.LayoutParams(-1, -1))
        surface.addView(button("Fit") { graph.fitToContent() }.apply {
            contentDescription = "Fit all saved devices and bindings"
        }, FrameLayout.LayoutParams(dp(46), dp(30), Gravity.TOP or Gravity.END).apply {
            topMargin = dp(4); rightMargin = dp(45)
        })
        val inspectorArea = FrameLayout(this)
        surface.addView(inspectorArea, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))
        val focusLabel = label("", 11f)
        surface.addView(focusLabel, FrameLayout.LayoutParams(-2, dp(28), Gravity.TOP or Gravity.CENTER_HORIZONTAL).apply {
            topMargin = dp(4)
        })
        fun updateInspector() {
            inspectorArea.removeAllViews()
            val device = guide.devices.firstOrNull { it.id == selectedGraphId } ?: return
            val details = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = background("#26334E", 10)
            }
            details.addView(button("×  ${device.name} · close") {
                selectedGraphId = null
                inspectorArea.removeAllViews()
            })
            details.addView(label("${device.type} — configured options", 12f, true))
            device.options.forEach { details.addView(label("${it.name}: ${it.value}", 12f)) }
            device.notes?.takeIf { it.isNotBlank() }?.let { details.addView(label("Note: $it", 12f)) }
            val links = BlueprintConnections.touching(guide, device.id)
            details.addView(label("Bindings (${links.size})", 12f, true))
            links.forEach { link -> details.addView(label("${link.sourceName} / ${link.eventName} → ${link.targetName} / ${link.functionName}", 12f)) }
            details.addView(button("Explore in Links") { linkSelectedId = device.id; switchTab(1) })
            val scroll = ScrollView(this).apply { isFillViewport = false; addView(details) }
            val maxHeight = ((params?.height ?: dp(250)) * 0.48f).toInt().coerceAtLeast(dp(85))
            inspectorArea.addView(scroll, FrameLayout.LayoutParams(-1, maxHeight))
        }
        graph.onNodeTap = { id -> selectedGraphId = id; updateInspector() }
        graph.onFocusChanged = { id ->
            focusedGraphId = id
            focusLabel.text = if (id == null) "" else "Focused: ${guide.devices.firstOrNull { it.id == id }?.name ?: "Device"}"
        }
        updateInspector()
        graph.onFocusChanged(focusedGraphId)
        if (guide.devices.isEmpty()) surface.addView(label("Add devices to see the saved graph."),
            FrameLayout.LayoutParams(-2, -2, Gravity.CENTER))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Refit only the overlay window: state and checked steps are retained.
        if (root != null) showWindow(expanded)
    }
    override fun onDestroy() {
        dismissNavigator()
        root?.let { try { manager.removeViewImmediate(it) } catch (_: Exception) { } }
        root = null; params = null
        work.cancel()
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
    private var moving = false
    private var focusId: String? = null
    private var highlighted: Set<String>? = null
    var onNodeTap: (String?) -> Unit = {}
    var onFocusChanged: (String?) -> Unit = {}

    fun setFocus(id: String?) {
        focusId = id?.takeIf { value -> nodes.containsKey(value) }
        highlighted = focusId?.let { BlueprintConnections.directlyConnected(blueprint, it) }
        invalidate()
    }
    private fun hitNode(x: Float, y: Float): String? {
        if (!::blueprint.isInitialized || zoom <= 0f) return null
        val graphX = (x - panX) / zoom
        val graphY = (y - panY) / zoom
        return blueprint.devices.asReversed().firstOrNull { device ->
            graphX >= device.graphX && graphX <= device.graphX + 176f &&
                graphY >= device.graphY && graphY <= device.graphY + 66f
        }?.id
    }
    private val gestures = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean = true
        override fun onLongPress(event: MotionEvent) {
            if (!scaler.isInProgress) hitNode(event.x, event.y)?.let { id ->
                setFocus(id)
                onFocusChanged(id)
                performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            }
        }
        override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
            if (moving) return false
            onNodeTap(hitNode(event.x, event.y))
            return true
        }
        override fun onDoubleTap(event: MotionEvent): Boolean {
            if (hitNode(event.x, event.y) == null) {
                setFocus(null)
                onFocusChanged(null)
                return true
            }
            return false
        }
    })
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
        if (focusId !in nodes.keys) focusId = null
        highlighted = focusId?.let { BlueprintConnections.directlyConnected(value, it) }
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
        if (oldw == 0 || oldh == 0) fitToContent()
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
        val focused = focusId
        val neighbors = highlighted
        canvas.withTranslation(panX, panY) {
            withScale(zoom, zoom) {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.4f
                blueprint.bindings.forEach { link ->
                    val a = nodes[link.sourceId]; val b = nodes[link.targetId]
                    if (a != null && b != null) {
                        val direct = focused == null || link.sourceId == focused || link.targetId == focused
                        paint.color = if (direct) Color.rgb(110, 218, 246) else Color.rgb(57, 68, 85)
                        drawLine(a.graphX + 176f, a.graphY + 33f,
                            b.graphX, b.graphY + 33f, paint)
                    }
                }
                blueprint.devices.forEach { device ->
                    val emphasized = neighbors == null || device.id in neighbors
                    val central = device.id == focused
                    nodeBounds.set(device.graphX, device.graphY, device.graphX + 176f, device.graphY + 66f)
                    paint.style = Paint.Style.FILL
                    paint.color = if (emphasized) Color.rgb(53, 70, 107) else Color.rgb(34, 39, 51)
                    drawRoundRect(nodeBounds, 10f, 10f, paint)
                    paint.style = Paint.Style.STROKE
                    paint.color = if (central) Color.rgb(228, 244, 255)
                        else if (emphasized) Color.rgb(123, 168, 237) else Color.rgb(61, 70, 86)
                    paint.strokeWidth = if (central) 3.4f else 1.7f
                    drawRoundRect(nodeBounds, 10f, 10f, paint)
                    paint.style = Paint.Style.FILL
                    paint.color = if (emphasized) Color.WHITE else Color.rgb(96, 106, 123)
                    paint.textSize = 15f
                    drawText(device.name, 0, min(device.name.length, 20),
                        device.graphX + 9f, device.graphY + 26f, paint)
                    paint.textSize = 12f
                    paint.color = if (emphasized) Color.rgb(205, 221, 247) else Color.rgb(96, 106, 123)
                    drawText(device.type, 0, min(device.type.length, 24),
                        device.graphX + 9f, device.graphY + 48f, paint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaler.onTouchEvent(event)
        gestures.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                lastX = event.x; lastY = event.y
                moving = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1 && !scaler.isInProgress) {
                    if (abs(event.x - lastX) > 2f || abs(event.y - lastY) > 2f) moving = true
                    if (moving) {
                        panX += event.x - lastX
                        panY += event.y - lastY
                        invalidate()
                    }
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

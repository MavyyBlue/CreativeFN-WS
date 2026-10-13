package dev.creativelogic.mobile.overlay

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.test.platform.app.InstrumentationRegistry
import dev.creativelogic.graph.Blueprint
import dev.creativelogic.graph.BlueprintBinding
import dev.creativelogic.graph.BlueprintDevice
import dev.creativelogic.graph.BlueprintOption
import org.junit.Assert.assertTrue
import org.junit.Test

/** The expanded details must be painted as part of the graph, never as a bottom sheet. */
class InlineNodeExpansionRenderTest {
    @Test fun expandedNodePaintsItsOwnDetailsThenCanReturnToCompact() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val blueprint = Blueprint("test", "Expansion", emptyList(), listOf(
                BlueprintDevice("trigger", "Trigger_1", "Trigger", 24f, 24f,
                    listOf(BlueprintOption("Enabled on Game Start", "On")), null, ""),
                BlueprintDevice("hud", "HUD_1", "HUD Message", 304f, 24f, emptyList(), null, ""),
            ), listOf(BlueprintBinding("trigger", "hud", "Trigger_1", "On Triggered", "HUD_1", "Show")),
                emptyList(), emptyList())
            val view = BlueprintGraphView(instrumentation.targetContext).apply { bind(blueprint) }
            view.measure(View.MeasureSpec.makeMeasureSpec(720, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(460, View.MeasureSpec.EXACTLY))
            view.layout(0, 0, 720, 460)
            fun filledPixels(): Int {
                val bitmap = Bitmap.createBitmap(720, 460, Bitmap.Config.ARGB_8888)
                view.draw(Canvas(bitmap))
                val expandedFill = Color.rgb(40, 58, 88)
                var hits = 0
                for (y in 0 until 460 step 3) for (x in 0 until 720 step 3) {
                    if (bitmap.getPixel(x, y) == expandedFill) hits++
                }
                bitmap.recycle()
                return hits
            }
            assertTrue("Compact graph must not contain expanded card fill", filledPixels() == 0)
            view.setExpanded("trigger")
            assertTrue("Expanded node should paint its own tall settings card", filledPixels() > 500)
            view.setExpanded(null)
            assertTrue("Closing must restore compact node rendering", filledPixels() == 0)
        }
    }
}

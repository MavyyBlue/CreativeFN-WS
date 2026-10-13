package dev.creativelogic.mobile.overlay

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.test.platform.app.InstrumentationRegistry
import dev.creativelogic.graph.Blueprint
import dev.creativelogic.graph.BlueprintBinding
import dev.creativelogic.graph.BlueprintDevice
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards against the blank Graph tab observed on the owner's landscape Fortnite test. */
class BlueprintGraphRenderTest {
    @Test fun graphRendersBothDeviceNodesAfterMeasuring() {
        val app = InstrumentationRegistry.getInstrumentation().targetContext
        val guide = Blueprint(
            mechanicId = "test", name = "Render test", quantities = emptyList(),
            devices = listOf(
                BlueprintDevice("first", "Trigger_1", "Trigger", 24f, 24f, emptyList(), null, ""),
                BlueprintDevice("second", "HUD Message_1", "HUD Message", 304f, 24f, emptyList(), null, ""),
            ),
            bindings = listOf(BlueprintBinding("first", "second", "Trigger_1", "On Triggered", "HUD Message_1", "Show")),
            steps = listOf("Place devices"), warnings = emptyList(),
        )
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val view = BlueprintGraphView(app).apply { bind(guide) }
            view.measure(View.MeasureSpec.makeMeasureSpec(720, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(340, View.MeasureSpec.EXACTLY))
            view.layout(0, 0, 720, 340)
            val bitmap = Bitmap.createBitmap(720, 340, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            val nodeFill = Color.rgb(53, 70, 107)
            var filledPixels = 0
            for (y in 0 until 340 step 4) for (x in 0 until 720 step 4) {
                if (bitmap.getPixel(x, y) == nodeFill) filledPixels++
            }
            assertTrue("Graph must show visible device cards after fit", filledPixels > 350)
            bitmap.recycle()
        }
    }
}

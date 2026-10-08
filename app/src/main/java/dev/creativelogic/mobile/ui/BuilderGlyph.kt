package dev.creativelogic.mobile.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp

/** Small, local stroke icons; adjacent text/buttons supply accessible names. */
enum class Glyph { Home, Library, Learn, Add, Arrow, Back, Search, Graph, Menu, Recent }

@Composable
fun BuilderGlyph(glyph: Glyph, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) {
            fun line(x: Float, y: Float, toX: Float, toY: Float) =
                drawLine(color, Offset(x, y), Offset(toX, toY), 1.8f, StrokeCap.Round)
            when (glyph) {
                Glyph.Recent -> {
                    drawCircle(color, 8f, Offset(12f, 12f), style = Stroke(1.8f))
                    line(12f, 7f, 12f, 12f); line(12f, 12f, 16f, 14f)
                }
                Glyph.Menu -> { line(4f, 6f, 20f, 6f); line(4f, 12f, 20f, 12f); line(4f, 18f, 20f, 18f) }
                Glyph.Add -> { line(12f, 5f, 12f, 19f); line(5f, 12f, 19f, 12f) }
                Glyph.Arrow -> { line(9f, 6f, 15f, 12f); line(15f, 12f, 9f, 18f) }
                Glyph.Back -> { line(15f, 6f, 9f, 12f); line(9f, 12f, 15f, 18f) }
                Glyph.Search -> {
                    drawCircle(color, 6f, Offset(10f, 10f), style = Stroke(1.8f))
                    line(15f, 15f, 21f, 21f)
                }
                Glyph.Home -> {
                    val path = Path().apply {
                        moveTo(3f, 11f); lineTo(12f, 4f); lineTo(21f, 11f)
                        moveTo(6f, 10f); lineTo(6f, 20f); lineTo(10f, 20f)
                        lineTo(10f, 14f); lineTo(14f, 14f); lineTo(14f, 20f)
                        lineTo(18f, 20f); lineTo(18f, 10f)
                    }
                    drawPath(path, color, style = Stroke(1.8f, cap = StrokeCap.Round))
                }
                Glyph.Library -> {
                    drawRoundRect(color, Offset(4f, 8f), Size(16f, 13f), CornerRadius(2f), style = Stroke(1.8f))
                    line(6f, 4f, 18f, 4f); line(5f, 6f, 19f, 6f)
                }
                Glyph.Learn -> {
                    drawRoundRect(color, Offset(4f, 4f), Size(16f, 16f), CornerRadius(4f), style = Stroke(1.8f))
                    line(12f, 11f, 12f, 16f)
                    drawCircle(color, 1f, Offset(12f, 8f))
                }
                Glyph.Graph -> {
                    line(7f, 6f, 16f, 12f); line(7f, 18f, 16f, 12f)
                    for (point in listOf(Offset(5f, 5f), Offset(5f, 19f), Offset(18f, 12f))) {
                        drawRoundRect(color, point - Offset(2.5f, 2.5f), Size(5f, 5f), CornerRadius(1.2f))
                    }
                }
            }
        }
    }
}

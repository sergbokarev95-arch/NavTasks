package com.navtasks.glasslab

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Pixel-identical vector strokes for the four production NavTasks 0.4.59 dock
 * icons, copied from NavGlassGlyph without introducing an app-module dependency.
 */
@Composable
internal fun GlassLabDockGlyph(kind: String, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val s = size.minDimension / 30f
        fun pt(x: Float, y: Float) = Offset(x * s, y * s)
        val width = 2.15f * s
        when (kind) {
            "today" -> {
                drawCircle(color, radius = 11f * s, center = pt(15f,15f), style = Stroke(width))
                val check = Path().apply {
                    moveTo(8.8f*s, 15f*s)
                    lineTo(13.1f*s, 19f*s)
                    lineTo(21.5f*s, 10.7f*s)
                }
                drawPath(check, color, style = Stroke(width = 2.6f*s, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            "work" -> {
                // A work case, distinct from the Tasks checklist glyph.
                drawRoundRect(color, topLeft = pt(3.5f,10f),
                    size = Size(23f*s,16f*s),
                    cornerRadius = CornerRadius(3f*s),
                    style = Stroke(width))
                drawRoundRect(color, topLeft = pt(11f,5f),
                    size = Size(8f*s,5f*s),
                    cornerRadius = CornerRadius(2f*s),
                    style = Stroke(width))
                drawLine(color, pt(3.5f,17f), pt(26.5f,17f), width,
                    cap = StrokeCap.Round)
                drawLine(color, pt(14f,17f), pt(16f,17f), 3f*s,
                    cap = StrokeCap.Round)
            }
            "handover" -> {
                // Distinct handover notebook icon, not the same glyph as Tasks.
                drawRoundRect(color, topLeft = pt(5f,4f), size = Size(20f*s,23f*s),
                    cornerRadius = CornerRadius(3.1f*s), style = Stroke(width))
                drawLine(color, pt(9f,10f), pt(15f,10f), width, cap = StrokeCap.Round)
                drawLine(color, pt(9f,15f), pt(16f,15f), width, cap = StrokeCap.Round)
                drawLine(color, pt(9f,20f), pt(15f,20f), width, cap = StrokeCap.Round)
                drawLine(color, pt(18f,18f), pt(23f,18f), width, cap = StrokeCap.Round)
                drawLine(color, pt(20.5f,15.5f), pt(23f,18f), width, cap = StrokeCap.Round)
                drawLine(color, pt(20.5f,20.5f), pt(23f,18f), width, cap = StrokeCap.Round)
            }
            "tasks" -> {
                for ((i, y) in listOf(7f,15f,23f).withIndex()) {
                    drawCircle(color, radius = 2.2f*s, center = pt(6.5f,y), style = Stroke(width))
                    drawLine(color, pt(13f,y), pt(if (i == 2) 23f else 26f,y), width,
                        cap = StrokeCap.Round)
                }
            }
        }
    }
}

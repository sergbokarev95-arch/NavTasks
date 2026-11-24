package com.navtasks.app

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Four dock-only, solid silhouettes inspired by HONOR Calendar.
 *
 * Internal marks are real transparent cutouts, not a second color painted
 * over the lens. Consequently the icons stay vivid on every refractive
 * background, while all existing global NavGlassGlyph icons stay untouched.
 */
@Composable
internal fun NavDockFilledGlyph(
    kind: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.graphicsLayer {
        compositingStrategy = CompositingStrategy.Offscreen
    }) {
        val unit = size.minDimension / 30f
        fun point(x: Float, y: Float) = Offset(x * unit, y * unit)
        fun cutLine(x1: Float, y1: Float, x2: Float, y2: Float, weight: Float) {
            drawLine(
                Color.Transparent,
                start = point(x1, y1), end = point(x2, y2),
                strokeWidth = weight * unit,
                cap = StrokeCap.Round,
                blendMode = BlendMode.Clear,
            )
        }

        when (kind) {
            "today" -> {
                // Recognizable filled check badge instead of a thin ring.
                drawCircle(color, radius = 12f * unit, center = point(15f, 15f))
                val check = Path().apply {
                    moveTo(8.9f * unit, 15.5f * unit)
                    lineTo(13.2f * unit, 19.6f * unit)
                    lineTo(21.5f * unit, 10.7f * unit)
                }
                drawPath(
                    check, Color.Transparent,
                    style = Stroke(width = 3.4f * unit, cap = StrokeCap.Round),
                    blendMode = BlendMode.Clear,
                )
            }
            "work" -> {
                // Solid work briefcase with a separate thick handle and seam.
                drawRoundRect(
                    color, topLeft = point(3.4f, 10.3f),
                    size = Size(23.2f * unit, 16.5f * unit),
                    cornerRadius = CornerRadius(3.1f * unit),
                )
                drawRoundRect(
                    color, topLeft = point(10.3f, 4.5f),
                    size = Size(9.4f * unit, 8f * unit),
                    cornerRadius = CornerRadius(2.6f * unit),
                )
                drawRoundRect(
                    Color.Transparent,
                    topLeft = point(12.8f, 7f),
                    size = Size(4.4f * unit, 5.0f * unit),
                    cornerRadius = CornerRadius(.8f * unit),
                    blendMode = BlendMode.Clear,
                )
                cutLine(5f, 17.9f, 25f, 17.9f, 1.55f)
                drawRoundRect(
                    color, topLeft = point(13.5f, 16.5f),
                    size = Size(3f * unit, 3.3f * unit),
                    cornerRadius = CornerRadius(.7f * unit),
                )
            }
            "handover" -> {
                // Filled transfer document, transparent list and transfer arrow.
                drawRoundRect(
                    color, topLeft = point(5f, 3.2f),
                    size = Size(20f * unit, 23.8f * unit),
                    cornerRadius = CornerRadius(3.3f * unit),
                )
                cutLine(9.7f, 10f, 18.2f, 10f, 2f)
                cutLine(9.7f, 14.6f, 17.3f, 14.6f, 2f)
                cutLine(9.7f, 19.2f, 15.2f, 19.2f, 2f)
                cutLine(17.4f, 22f, 22f, 22f, 1.9f)
                cutLine(19.9f, 19.7f, 22f, 22f, 1.9f)
                cutLine(19.9f, 24.3f, 22f, 22f, 1.9f)
            }
            "reference" -> {
                // Two solid book pages, with a clear gutter and page accents.
                val left = Path().apply {
                    moveTo(15f * unit, 8f * unit)
                    lineTo(9.4f * unit, 5.5f * unit)
                    lineTo(3.3f * unit, 6.6f * unit)
                    lineTo(3.3f * unit, 24.6f * unit)
                    lineTo(9.6f * unit, 23.4f * unit)
                    lineTo(15f * unit, 26.3f * unit)
                    close()
                }
                val right = Path().apply {
                    moveTo(15f * unit, 8f * unit)
                    lineTo(20.6f * unit, 5.5f * unit)
                    lineTo(26.7f * unit, 6.6f * unit)
                    lineTo(26.7f * unit, 24.6f * unit)
                    lineTo(20.4f * unit, 23.4f * unit)
                    lineTo(15f * unit, 26.3f * unit)
                    close()
                }
                drawPath(left, color)
                drawPath(right, color)
                cutLine(15f, 8.1f, 15f, 25.9f, 1.8f)
                cutLine(6.4f, 10.7f, 11.8f, 10f, 1.15f)
                cutLine(18.2f, 10f, 23.6f, 10.7f, 1.15f)
            }
            else -> {
                // Never swallow a new dock icon identifier silently.
                drawCircle(color, radius = 8f * unit, center = point(15f, 15f))
            }
        }
    }
}

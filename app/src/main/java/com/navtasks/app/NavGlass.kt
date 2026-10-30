package com.navtasks.app

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border

/** Local, lightweight light fields make the translucent layers visibly react to their own backdrop. */
@Composable
fun NavGlassBackdrop(p: NavPalette) {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(p.backgroundEnd, p.background)))
        val glow = if (p.dark) 0.12f else 0.075f
        val cx = size.width * 0.92f
        val cy = size.height * 0.17f
        val radius = size.width * 0.95f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(p.blue.copy(alpha = glow), Color.Transparent),
                center = Offset(cx, cy), radius = radius,
            ),
            center = Offset(cx, cy), radius = radius,
        )
        val low = Offset(size.width * 0.03f, size.height * 0.93f)
        val lowRadius = size.width * 0.73f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(p.mint.copy(alpha = glow * 0.52f), Color.Transparent),
                center = low, radius = lowRadius,
            ),
            center = low, radius = lowRadius,
        )
    }
}

/**
 * NavTasks Glass is an app-owned, vendor-neutral visual system.
 * It deliberately does NOT depend on private HONOR APIs or on screenshotting the wallpaper.
 * The translucent fill, gradient illumination, edge highlight and shadow all work on
 * stock Android and vendor launchers alike. The translucent effect is not a live backdrop blur.
 */
enum class GlassRole { PANEL, TASK, NESTED_TASK, NAVIGATION, SELECTED, CONTROL }

fun Modifier.navGlass(
    p: NavPalette,
    radius: Dp = 20.dp,
    role: GlassRole = GlassRole.PANEL,
    elevation: Dp = 4.dp,
): Modifier {
    val shape = RoundedCornerShape(radius)
    val fill = when (role) {
        GlassRole.PANEL -> if (p.dark) Color(0xBC1D3652) else Color(0xDFFBFDFF)
        GlassRole.TASK -> if (p.dark) Color(0xD31A3049) else Color(0xE9FFFFFF)
        GlassRole.NESTED_TASK -> if (p.dark) Color(0xDB214A67) else Color(0xE1DCEFFB)
        GlassRole.NAVIGATION -> if (p.dark) Color(0xE11C2D43) else Color(0xE7F7FBFF)
        GlassRole.SELECTED -> if (p.dark) Color(0xBC527FA8) else Color(0xC5BDDDF4)
        GlassRole.CONTROL -> if (p.dark) Color(0xBA37516C) else Color(0xD6F6FBFF)
    }
    val tint = if (p.dark) Color(0xFFB3DEFF) else Color(0xFF558CB2)
    val highlight = if (p.dark) Color.White.copy(alpha = 0.19f) else Color.White.copy(alpha = 0.90f)
    val lower = if (p.dark) Color(0xFF070F1B).copy(alpha = 0.16f) else Color(0xFF80A9C8).copy(alpha = 0.08f)
    return this
        .shadow(elevation, shape, clip = false,
            ambientColor = Color.Black.copy(alpha = if (p.dark) 0.23f else 0.09f),
            spotColor = Color.Black.copy(alpha = if (p.dark) 0.28f else 0.11f))
        .clip(shape)
        .background(fill, shape)
        .background(
            Brush.linearGradient(listOf(highlight, tint.copy(alpha = 0.035f), lower)), shape
        )
        .border(
            1.dp,
            Brush.linearGradient(
                listOf(
                    if (p.dark) Color.White.copy(alpha = 0.42f) else Color.White,
                    tint.copy(alpha = if (p.dark) 0.25f else 0.30f),
                    tint.copy(alpha = if (p.dark) 0.15f else 0.23f),
                )
            ),
            shape,
        )
}

@Composable
fun NavGlassBottomBar(p: NavPalette, selected: Int, lang: UiLanguage, onSelect: (Int) -> Unit) {
    Box(
        Modifier.fillMaxWidth().navigationBarsPadding()
            .padding(start = 15.dp, end = 15.dp, top = 7.dp, bottom = 9.dp),
    ) {
        BoxWithConstraints(
            Modifier.fillMaxWidth().height(76.dp)
                .navGlass(p, radius = 30.dp, role = GlassRole.NAVIGATION, elevation = 10.dp)
                .padding(6.dp),
        ) {
            val segment = maxWidth / 3
            val slide by animateDpAsState(
                targetValue = segment * selected.coerceIn(0, 2),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.78f),
                label = "navGlassSelection",
            )
            Box(
                Modifier.offset(x = slide).width(segment).fillMaxHeight()
                    .navGlass(p, radius = 25.dp, role = GlassRole.SELECTED, elevation = 0.dp),
            )
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                val names = listOf(
                    localized("Сегодня", "Today", lang),
                    localized("План", "Plan", lang),
                    localized("Задачи", "Tasks", lang),
                )
                repeat(3) { index ->
                    val active = selected == index
                    Column(
                        Modifier.weight(1f).fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { onSelect(index) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        NavGlassGlyph(
                            kind = when (index) { 0 -> "today"; 1 -> "calendar"; else -> "tasks" },
                            color = if (active) p.blue else p.text.copy(alpha = 0.89f),
                            modifier = Modifier.size(29.dp),
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(names[index], color = if (active) p.blue else p.muted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun NavGlassCircleButton(p: NavPalette, kind: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).navGlass(p, radius = 22.dp, role = GlassRole.CONTROL)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NavGlassGlyph(kind, p.text, Modifier.size(25.dp))
    }
}

/** Lightweight vector navigation pictograms, no emoji and no extended icon package. */
@Composable
fun NavGlassGlyph(kind: String, color: Color, modifier: Modifier = Modifier) {
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
            "calendar" -> {
                drawRoundRect(color, topLeft = pt(4f,6f), size = Size(22f*s,20f*s),
                    cornerRadius = CornerRadius(3.2f*s), style = Stroke(width))
                drawLine(color, pt(4f,12f), pt(26f,12f), width)
                drawLine(color, pt(10f,4f), pt(10f,9f), width, cap = StrokeCap.Round)
                drawLine(color, pt(20f,4f), pt(20f,9f), width, cap = StrokeCap.Round)
                for (x in listOf(10f,15f,20f)) for (y in listOf(17f,22f)) {
                    drawCircle(color, radius = 1.1f*s, center = pt(x,y))
                }
            }
            "tasks" -> {
                for ((i, y) in listOf(7f,15f,23f).withIndex()) {
                    drawCircle(color, radius = 2.2f*s, center = pt(6.5f,y), style = Stroke(width))
                    drawLine(color, pt(13f,y), pt(if (i == 2) 23f else 26f,y), width,
                        cap = StrokeCap.Round)
                }
            }
            "back" -> {
                val path = Path().apply {
                    moveTo(19f*s,5f*s)
                    lineTo(9f*s,15f*s)
                    lineTo(19f*s,25f*s)
                }
                drawPath(path,color, style=Stroke(width=3.1f*s,cap=StrokeCap.Round,join=StrokeJoin.Round))
            }
            "settings" -> {
                // Crisp gear silhouette, no 3-D emoji.
                drawCircle(color, radius = 7.8f*s, center = pt(15f,15f), style = Stroke(width))
                drawCircle(color, radius = 2.7f*s, center = pt(15f,15f), style = Stroke(width))
                for (n in 0 until 8) {
                    val a = n * Math.PI / 4.0
                    val dx = kotlin.math.cos(a).toFloat()
                    val dy = kotlin.math.sin(a).toFloat()
                    drawLine(color, pt(15f + dx*10f,15f + dy*10f),
                        pt(15f + dx*13f,15f + dy*13f),width * 1.3f, cap = StrokeCap.Round)
                }
            }
        }
    }
}

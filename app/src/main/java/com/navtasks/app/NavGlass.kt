package com.navtasks.app

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle

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
 * NavTasks Glass uses a restrained neutral translucent material on every Android device.
 * Individual controls do not have a frosted-white or blue cloudy film. Live wallpaper
 * blurring is intentionally not promised: vendor launchers do not expose it consistently.
 */
enum class GlassRole { PANEL, TASK, NESTED_TASK, NAVIGATION, SELECTED, CONTROL }

fun Modifier.navGlass(
    p: NavPalette,
    radius: Dp = 20.dp,
    role: GlassRole = GlassRole.PANEL,
    elevation: Dp = 0.dp, // Kept for existing call sites; material draws NO shadow.
): Modifier {
    val shape = RoundedCornerShape(radius)
    val fill = when (role) {
        GlassRole.PANEL -> if (p.dark) Color(0x651F3143) else Color(0x3AFFFFFF)
        GlassRole.TASK -> if (p.dark) Color(0x571C2D40) else Color(0x2BFFFFFF)
        GlassRole.NESTED_TASK -> if (p.dark) Color(0x5C234259) else Color(0x2DDAEAF3)
        GlassRole.NAVIGATION -> if (p.dark) Color(0xA0152433) else Color(0x77FFFFFF)
        GlassRole.SELECTED -> if (p.dark) Color(0x394C6478) else Color(0x3AC5D8E4)
        GlassRole.CONTROL -> if (p.dark) Color(0x442C3D50) else Color(0x24FFFFFF)
    }
    // The edge, not a white/blue blanket on the surface, communicates the glass.
    val edge = if (p.dark) Color.White.copy(alpha = 0.34f)
               else Color(0xFF83AAC2).copy(alpha = 0.44f)
    val topGlint = if (p.dark) Color.White.copy(alpha = 0.32f)
                   else Color.White.copy(alpha = 0.79f)
    val bottomEdge = if (p.dark) Color(0xFF54778D).copy(alpha = 0.35f)
                     else Color(0xFF92B0C2).copy(alpha = 0.31f)
    return this
        .clip(shape)
        .background(fill, shape)
        .border(1.dp, Brush.verticalGradient(listOf(topGlint, edge, bottomEdge)), shape)
}

/** A single sliding glass lens. No drop shadow, glow or second moving surface. */
fun Modifier.navGlassSelection(p: NavPalette, radius: Dp = 23.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    val fill = if (p.dark) Color(0x4A8798A8) else Color(0x4FC7D8E4)
    return this
        .clip(shape)
        .background(fill, shape)
        .border(
            1.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = if (p.dark) 0.57f else 0.92f),
                    Color(0xFFB7D2E1).copy(alpha = if (p.dark) 0.23f else 0.45f),
                    Color(0xFF78A9C5).copy(alpha = if (p.dark) 0.48f else 0.38f),
                ),
            ), shape,
        )
}

@Composable
fun NavGlassBottomBar(p: NavPalette, selected: Int, lang: UiLanguage, onSelect: (Int) -> Unit) {
    Box(
        Modifier.fillMaxWidth().navigationBarsPadding()
            .padding(start = 15.dp, end = 15.dp, top = 3.dp, bottom = 5.dp),
    ) {
        BoxWithConstraints(
            Modifier.fillMaxWidth().height(56.dp)
                .navGlass(p, radius = 30.dp, role = GlassRole.NAVIGATION, elevation = 0.dp)
                .padding(4.dp),
        ) {
            val segment = maxWidth / 3
            val slide by animateDpAsState(
                targetValue = segment * selected.coerceIn(0, 2),
                animationSpec = tween(durationMillis = 210, easing = FastOutSlowInEasing),
                label = "navSingleGlassLens",
            )
            Box(
                Modifier.offset(x = slide).width(segment).fillMaxHeight()
                    .navGlassSelection(p, radius = 23.dp),
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
                            .clip(RoundedCornerShape(21.dp))
                            .clickable { onSelect(index) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        NavGlassGlyph(
                            kind = when (index) { 0 -> "today"; 1 -> "calendar"; else -> "tasks" },
                            color = if (active) p.blue else p.text.copy(alpha = 0.89f),
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(names[index], color = if (active) p.blue else p.muted, fontSize = 10.sp)
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

/** Finger-friendly create button, anchored just above the navigation capsule. */
@Composable
fun NavGlassAddButton(p: NavPalette, onClick: () -> Unit) {
    Box(
        Modifier.size(54.dp)
            .navGlass(p, radius = 27.dp, role = GlassRole.SELECTED, elevation = 0.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NavGlassGlyph("plus", p.blue, Modifier.size(28.dp))
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
            "plus" -> {
                drawLine(color, pt(15f, 6f), pt(15f, 24f), width * 1.2f, cap = StrokeCap.Round)
                drawLine(color, pt(6f, 15f), pt(24f, 15f), width * 1.2f, cap = StrokeCap.Round)
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

/** Reusable compact glass input: unlike Material OutlinedTextField it does not reserve 56dp. */
@Composable
fun NavGlassInput(
    value: String,
    onValueChange: (String) -> Unit,
    p: NavPalette,
    placeholder: String = "",
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(42.dp)
            .navGlass(p, radius = 12.dp, role = GlassRole.CONTROL, elevation = 0.dp),
        singleLine = true,
        textStyle = TextStyle(color = p.text, fontSize = 14.sp),
        cursorBrush = SolidColor(p.blue),
        decorationBox = { input ->
            Box(Modifier.fillMaxSize().padding(horizontal = 13.dp), contentAlignment = Alignment.CenterStart) {
                if (value.isBlank() && placeholder.isNotEmpty())
                    Text(placeholder, color = p.muted, fontSize = 13.sp)
                input()
            }
        },
    )
}

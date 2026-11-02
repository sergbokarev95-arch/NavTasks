package com.navtasks.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.geometry.Rect
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.math.PI

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
 * Static glass cards stay lightweight. Only three floating bottom controls use
 * shared real Haze backdrop blur, with the scrolling content as the source.
 * We never sample other apps or device wallpaper.
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
        GlassRole.PANEL -> if (p.dark) Color(0x851F3143) else Color(0x85FFFFFF)
        GlassRole.TASK -> if (p.dark) Color(0x781C2D40) else Color(0x79FFFFFF)
        GlassRole.NESTED_TASK -> if (p.dark) Color(0x80234259) else Color(0x7BDDECF7)
        GlassRole.NAVIGATION -> if (p.dark) Color(0x7918202A) else Color(0x7AFFFFFF)
        GlassRole.SELECTED -> if (p.dark) Color(0x582989C5) else Color(0x493EA1F2)
        GlassRole.CONTROL -> if (p.dark) Color(0x792C3D50) else Color(0x80FFFFFF)
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

/**
 * True frosted-glass backdrop for the bottom navigation and its two floating
 * sibling buttons. The lower page is already registered with hazeSource(state).
 * Keep a single source and reuse it here to avoid extra offscreen captures.
 * The blur is drawn underneath crisp icons/labels and clipped to the lens.
 */
fun Modifier.navFrostedOverlay(
    p: NavPalette,
    hazeState: HazeState,
    radius: Dp,
    role: GlassRole,
): Modifier {
    val shape = RoundedCornerShape(radius)
    return this
        .clip(shape)
        .hazeEffect(
            state = hazeState,
            style = HazeStyle(
                backgroundColor = if (p.dark) Color(0x68172739) else Color(0x74EDF5FA),
                blurRadius = 34.dp,
                tints = listOf(
                    HazeTint(if (p.dark) Color(0x24141E2B) else Color(0x16FFFFFF))
                ),
                noiseFactor = 0.025f,
            ),
        )
        .navGlass(p, radius = radius, role = role, elevation = 0.dp)
}

/**
 * An accessible click target without the platform's transient ripple or grey pressed film.
 * Necessary for clear glass: the moving lens is the only visible touch feedback.
 */
@Composable
fun Modifier.glassClickable(onClick: () -> Unit): Modifier {
    val view = LocalView.current
    return this.clickable(interactionSource = null, indication = null) {
        // Honours the phone's system haptic-feedback setting (no forced vibration).
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        onClick()
    }
}

/** A single sliding glass lens. No drop shadow, glow or second moving surface. */
fun Modifier.navGlassSelection(p: NavPalette, radius: Dp = 23.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    val fill = if (p.dark) Color(0x2C7193A8) else Color(0x14FFFFFF)
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
fun NavGlassBottomBar(
    p: NavPalette,
    selected: Int,
    lang: UiLanguage,
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    onSelect: (Int) -> Unit,
) {
    Box(
        modifier.fillMaxWidth().navigationBarsPadding()
            .padding(start = 15.dp, end = 15.dp, top = 3.dp, bottom = 5.dp),
    ) {
        BoxWithConstraints(
            Modifier.fillMaxWidth().height(56.dp)
                .navFrostedOverlay(p, hazeState, radius = 30.dp, role = GlassRole.NAVIGATION)
                .padding(4.dp),
        ) {
            // Direct drawing makes one moving lens, without keeping an old composited
            // layer (the ghost users observed between tabs on some MagicOS devices).
            val slide by animateFloatAsState(
                targetValue = selected.coerceIn(0, 3).toFloat(),
                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                label = "navSingleGlassLens",
            )
            // Lens travels as clear glass first, then its crisp edge dissolves
            // into a quiet neutral-grey selected pill, as in MagicOS.
            // No grey flash BEFORE the lens moves.
            val matte = remember { Animatable(1f) }
            val droplet = remember { Animatable(0f) }
            var lastSelection by remember { mutableStateOf<Int?>(null) }
            LaunchedEffect(selected) {
                val wasSelected = lastSelection
                lastSelection = selected
                if (wasSelected == null || wasSelected == selected) {
                    matte.snapTo(1f)
                    droplet.snapTo(0f)
                } else {
                    // This is a light refraction falling onto the destination.
                    // It starts with the tap, while the ONE lens travels.
                    // The grey rest state only appears after the 240ms travel.
                    matte.snapTo(0f)
                    droplet.snapTo(0f)
                    launch {
                        droplet.animateTo(1f, tween(durationMillis = 335,
                            easing = FastOutSlowInEasing))
                        droplet.snapTo(0f)
                    }
                    delay(245L)
                    matte.animateTo(1f, tween(durationMillis = 245))
                }
            }
            Canvas(Modifier.matchParentSize()) {
                val cell = size.width / 4f
                // Same centered inset in Day/Night; no asymmetric lower shadow.
                val inset = 2.dp.toPx()
                // One uniformly inset lens; no extra pressed-state or old tab layer.
                val lensSize = Size(cell - 2f * inset, size.height - 2f * inset)
                val lensTopLeft = Offset(cell * slide + inset, inset)
                val lensCorner = CornerRadius(lensSize.height / 2f, lensSize.height / 2f)
                val settle = matte.value
                val movingFill = if (p.dark) Color(0x367193A8) else Color(0x24FFFFFF)
                val restingFill = if (p.dark) Color(0x56616A74) else Color(0x74D5D9DE)
                drawRoundRect(
                    color = lerp(movingFill, restingFill, settle),
                    topLeft = lensTopLeft, size = lensSize, cornerRadius = lensCorner,
                )
                val restingEdge = if (p.dark) Color(0xFF70808D).copy(alpha = 0.30f)
                    else Color(0xFFB5C1CB).copy(alpha = 0.33f)
                // A symmetric vertical edge removes Day-mode optical bottom
                // heaviness while retaining a bright edge during travel.
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            lerp(Color.White.copy(alpha = if (p.dark) 0.57f else 0.79f),
                                restingEdge, settle),
                            lerp(Color.White.copy(alpha = if (p.dark) 0.23f else 0.37f),
                                restingEdge, settle),
                            lerp(Color.White.copy(alpha = if (p.dark) 0.57f else 0.79f),
                                restingEdge, settle),
                        ),
                    ),
                    topLeft = lensTopLeft,
                    size = lensSize,
                    cornerRadius = lensCorner,
                    style = Stroke(width = 1.dp.toPx()),
                )

                // One soft falling bead and a circular impact ripple, clipped
                // by the dock. Pure light, never an extra moving grey pill.
                val phase = droplet.value
                if (phase > 0f && phase < 1f) {
                    val x = cell * (selected.coerceIn(0, 3) + 0.5f)
                    val centerY = size.height / 2f
                    val glint = if (p.dark) Color(0xFFA7DAFF) else Color(0xFFFAFFFF)
                    if (phase < 0.49f) {
                        val descent = phase / 0.49f
                        val y = size.height * (0.13f + 0.37f * descent)
                        val opacity = 0.25f + 0.35f * descent
                        drawOval(
                            color = glint.copy(alpha = opacity),
                            topLeft = Offset(x - 3.4.dp.toPx(), y - 5.2.dp.toPx()),
                            size = Size(6.8.dp.toPx(), 10.4.dp.toPx()),
                        )
                    } else {
                        val impact = (phase - 0.49f) / 0.51f
                        val fade = (1f - impact).coerceIn(0f, 1f)
                        val radius = (6f + impact * 23f).dp.toPx()
                        drawCircle(
                            color = glint.copy(alpha = fade * (if (p.dark) 0.33f else 0.62f)),
                            center = Offset(x, centerY),
                            radius = radius,
                            style = Stroke(width = (1.7f - 0.8f * impact).dp.toPx()),
                        )
                        val glow = sin(impact.toDouble() * PI).toFloat().coerceAtLeast(0f) * fade
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(glint.copy(alpha = glow * 0.18f), Color.Transparent),
                                center = Offset(x, centerY),
                                radius = radius * 1.3f,
                            ),
                            center = Offset(x, centerY), radius = radius * 1.3f,
                        )
                    }
                }
            }
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                val names = listOf(
                    localized("Сегодня", "Today", lang),
                    localized("План", "Plan", lang),
                    "Handover",
                    localized("Задачи", "Tasks", lang),
                )
                repeat(4) { index ->
                    val active = selected == index
                    // Strong blue belongs to the glyph and caption, not the glass lens.
                    val activeBlue = if (p.dark) Color(0xFF329CFF) else Color(0xFF087DFA)
                    Column(
                        Modifier.weight(1f).fillMaxHeight()
                            .clip(RoundedCornerShape(21.dp))
                            .glassClickable { onSelect(index) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        NavGlassGlyph(
                            kind = when (index) {
                                0 -> "today"
                                1 -> "calendar"
                                2 -> "handover"
                                else -> "tasks"
                            },
                            color = if (active) activeBlue else p.text.copy(alpha = 0.89f),
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(names[index], color = if (active) activeBlue else p.muted, fontSize = 10.sp)
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
            .glassClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NavGlassGlyph(kind, p.text, Modifier.size(25.dp))
    }
}

/** Finger-friendly create button, anchored just above the navigation capsule. */
@Composable
fun NavGlassAddButton(p: NavPalette, hazeState: HazeState, onClick: () -> Unit) {
    Box(
        Modifier.size(54.dp)
            .navFrostedOverlay(p, hazeState, radius = 27.dp, role = GlassRole.CONTROL)
            .glassClickable(onClick = onClick),
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
            "back" -> {
                val path = Path().apply {
                    moveTo(19f*s,5f*s)
                    lineTo(9f*s,15f*s)
                    lineTo(19f*s,25f*s)
                }
                drawPath(path,color, style=Stroke(width=3.1f*s,cap=StrokeCap.Round,join=StrokeJoin.Round))
            }
            "chevron-down", "chevron-right" -> {
                val path = Path().apply {
                    if (kind == "chevron-down") {
                        moveTo(7f*s, 12f*s)
                        lineTo(15f*s, 19f*s)
                        lineTo(23f*s, 12f*s)
                    } else {
                        moveTo(11f*s, 6f*s)
                        lineTo(19f*s, 15f*s)
                        lineTo(11f*s, 24f*s)
                    }
                }
                drawPath(path, color, style = Stroke(width = 2.5f*s,
                    cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            "plus" -> {
                drawLine(color, pt(15f, 6f), pt(15f, 24f), width * 1.2f, cap = StrokeCap.Round)
                drawLine(color, pt(6f, 15f), pt(24f, 15f), width * 1.2f, cap = StrokeCap.Round)
            }
            "settings" -> {
                // A real toothed cog with a cut-out center (not the former sunburst).
                val cog = Path().apply {
                    fillType = PathFillType.EvenOdd
                    for (tooth in 0 until 8) {
                        val center = -Math.PI / 2 + tooth * Math.PI / 4
                        val samples = listOf(
                            Pair(-0.39, 9.8f), Pair(-0.25, 12.8f),
                            Pair(0.25, 12.8f), Pair(0.39, 9.8f),
                        )
                        for ((index, sample) in samples.withIndex()) {
                            val angle = center + sample.first
                            val x = (15f + kotlin.math.cos(angle).toFloat() * sample.second) * s
                            val y = (15f + kotlin.math.sin(angle).toFloat() * sample.second) * s
                            if (tooth == 0 && index == 0) moveTo(x, y) else lineTo(x, y)
                        }
                    }
                    close()
                    addOval(Rect(pt(15f - 3.9f, 15f - 3.9f), pt(15f + 3.9f, 15f + 3.9f)))
                }
                drawPath(cog, color)
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

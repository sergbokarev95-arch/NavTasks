package com.navtasks.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.graphicsLayer
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlin.math.abs

/** Local, lightweight light fields make the translucent layers visibly react to their own backdrop. */
@Composable
fun NavGlassBackdrop(p: NavPalette) {
    Canvas(Modifier.fillMaxSize()) {
        // OLED night mode: the backdrop is EXACTLY black everywhere outside
        // panels. No gradients, glows, or blended blue pixels.
        if (p.dark) {
            drawRect(Color.Black)
            return@Canvas
        }
        drawRect(Brush.verticalGradient(listOf(p.backgroundEnd, p.background)))
        val glow = 0.075f
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
    // Day dock's top border was almost white on white, while the bottom
    // remained grey. Use a single perceptually uniform neutral border.
    val dockDayEdge = Color(0xFF9DA9B5).copy(alpha = 0.48f)
    val edge = when {
        p.dark -> Color.White.copy(alpha = 0.34f)
        role == GlassRole.NAVIGATION -> dockDayEdge
        else -> Color(0xFF83AAC2).copy(alpha = 0.44f)
    }
    val topGlint = when {
        p.dark -> Color.White.copy(alpha = 0.32f)
        role == GlassRole.NAVIGATION -> dockDayEdge
        else -> Color.White.copy(alpha = 0.79f)
    }
    val bottomEdge = when {
        p.dark -> Color(0xFF54778D).copy(alpha = 0.35f)
        role == GlassRole.NAVIGATION -> dockDayEdge
        else -> Color(0xFF92B0C2).copy(alpha = 0.31f)
    }
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
                // Dock glass is neutral in Day mode; the previous icy blue
                // source tint was showing through the selected capsule.
                backgroundColor = if (p.dark) Color(0x68172739)
                    else if (role == GlassRole.NAVIGATION) Color(0x88F3F3F3)
                    else Color(0x74EDF5FA),
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
fun Modifier.glassClickable(onClick: () -> Unit): Modifier =
    glassClickable(interactionSource = null, onClick = onClick)

/** Optional press observation; click cancellation is still owned by Compose. */
@Composable
fun Modifier.glassClickable(
    interactionSource: MutableInteractionSource?,
    onClick: () -> Unit,
): Modifier {
    val view = LocalView.current
    return this.clickable(interactionSource = interactionSource, indication = null) {
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
                // HONOR screenshot: selected lens height ≈89% of dock height,
                // not the old 79% produced by 4dp padding + 2dp inset.
                .padding(horizontal = 1.dp, vertical = 2.dp),
        ) {
            // Retarget the live spring, retaining position and velocity on rapid taps.
            // Draw one lens directly so no stale composited selection is left behind.
            val target = selected.coerceIn(0, 3).toFloat()
            val slide by animateFloatAsState(
                targetValue = target,
                animationSpec = spring(dampingRatio = 1f, stiffness = 550f,
                    visibilityThreshold = 0.001f),
                label = "navSingleGlassLens",
            )
            val moving = abs(slide - target) > 0.015f
            // Material follows actual travel rather than a timer. Both effects
            // retarget from their current values instead of flashing back to zero.
            val matte by animateFloatAsState(
                targetValue = if (moving) 0f else 1f,
                animationSpec = spring(dampingRatio = 1f, stiffness = 700f),
                label = "navLensMatte",
            )
            val liquidStretch by animateFloatAsState(
                targetValue = if (moving) 1f else 0f,
                animationSpec = spring(dampingRatio = 1f, stiffness = 600f),
                label = "navLensStretch",
            )
            Canvas(Modifier.matchParentSize()) {
                val cell = size.width / 4f
                val inset = 1.dp.toPx()
                val stretch = liquidStretch.coerceIn(0f, 1f) *
                    minOf(10.dp.toPx(), cell * 0.13f)

                // Always use a true round rectangle. The previous hand-drawn
                // cubic path distorted the top/bottom edges and lost the pill
                // silhouette during movement, especially in Day mode.
                val pillHeight = size.height - inset * 2f
                val pillTop = (size.height - pillHeight) / 2f
                // Symmetric clearance, including when the first/last tab is
                // selected: 1dp parent horizontal padding + 2dp inset = 3dp,
                // matching 2dp parent vertical padding + 1dp inset = 3dp.
                // The selection must be narrower than a tab cell to remain
                // centered at both rounded ends without touching the dock.
                val horizontalInset = 2.dp.toPx()
                val pillWidth = cell - 2f * horizontalInset + stretch
                val nominalCenter = cell * (slide + 0.5f)
                val pillLeft = (nominalCenter - pillWidth / 2f)
                    .coerceIn(
                        horizontalInset,
                        (size.width - horizontalInset - pillWidth)
                            .coerceAtLeast(horizontalInset),
                    )
                val pillTopLeft = Offset(pillLeft, pillTop)
                val pillSize = Size(pillWidth, pillHeight)
                val pillRadius = CornerRadius(pillHeight / 2f, pillHeight / 2f)

                // During travel the single lens is unmistakably GLASS:
                // cool translucent material, stronger specular edges, and a
                // thin internal highlight. Only AFTER travel does it fade to
                // a symmetrical matte-grey resting selection.
                val settled = matte.coerceIn(0f, 1f)
                // Night material stays unchanged. Day uses neutral WHITE/GRAPHITE,
                // not blue; same translucent lens animation and draw geometry.
                // Identical top/bottom shades eliminate the weighted lower rim.
                val moveTop = if (p.dark) Color(0xA35A8CA9) else Color(0xC4FAFAFB)
                val moveMid = if (p.dark) Color(0x84507696) else Color(0xA9E2E3E5)
                val moveBottom = if (p.dark) Color(0xA33E647D) else Color(0xC4FAFAFB)
                val restingFill = if (p.dark) Color(0x89616B77) else Color(0xB2D7D8DA)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            lerp(moveTop, restingFill, settled),
                            lerp(moveMid, restingFill, settled),
                            lerp(moveBottom, restingFill, settled),
                        ),
                    ),
                    topLeft = pillTopLeft,
                    size = pillSize,
                    cornerRadius = pillRadius,
                )

                val movingEdgeTop = if (p.dark) Color(0xC7E3F8FF)
                    else Color(0xEFFFFFFF)
                val movingEdgeMid = if (p.dark) Color(0x896AAED0)
                    else Color(0xAC818A94)
                val restingEdge = if (p.dark) Color(0x547B8998)
                    else Color(0x8AA8ACB1)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            lerp(movingEdgeTop, restingEdge, settled),
                            lerp(movingEdgeMid, restingEdge, settled),
                            lerp(movingEdgeTop, restingEdge, settled),
                        ),
                    ),
                    topLeft = pillTopLeft,
                    size = pillSize,
                    cornerRadius = pillRadius,
                    style = Stroke(width = (1.2f - settled * 0.2f).dp.toPx()),
                )

                // This is a highlight ON the moving capsule, not an isolated
                // circle or droplet over the icon.
                if (settled < 0.99f) {
                    val inner = 2.dp.toPx()
                    val innerHeight = pillHeight - inner * 2f
                    val innerWidth = pillWidth - inner * 2f
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = (1f - settled) *
                                    (if (p.dark) 0.34f else 0.44f)),
                                Color.Transparent,
                                Color.White.copy(alpha = (1f - settled) *
                                    (if (p.dark) 0.15f else 0.44f)),
                            ),
                        ),
                        topLeft = Offset(pillLeft + inner, pillTop + inner),
                        size = Size(innerWidth, innerHeight),
                        cornerRadius = CornerRadius(innerHeight / 2f, innerHeight / 2f),
                        style = Stroke(width = 0.65.dp.toPx()),
                    )
                }
            }
            Row(Modifier.fillMaxSize().selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
                val names = listOf(
                    localized("Сегодня", "Today", lang),
                    localized("Работы", "Work", lang),
                    "Handover",
                    localized("Задачи", "Tasks", lang),
                )
                repeat(4) { index ->
                    val active = selected == index
                    val interaction = remember { MutableInteractionSource() }
                    val pressed by interaction.collectIsPressedAsState()
                    val pressScale by animateFloatAsState(
                        targetValue = if (pressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = 1f, stiffness = 1600f),
                        label = "navTabPress",
                    )
                    val view = LocalView.current
                    // Strong blue belongs to the glyph and caption, not the glass lens.
                    val activeBlue = if (p.dark) Color(0xFF329CFF) else Color(0xFF087DFA)
                    Column(
                        Modifier.weight(1f).fillMaxHeight()
                            .clip(RoundedCornerShape(21.dp))
                            .selectable(
                                selected = active,
                                role = Role.Tab,
                                interactionSource = interaction,
                                indication = null,
                            ) {
                                if (!active) {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    onSelect(index)
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        NavGlassGlyph(
                            kind = when (index) {
                                0 -> "today"
                                1 -> "work"
                                2 -> "handover"
                                else -> "tasks"
                            },
                            color = if (active) activeBlue else p.text.copy(alpha = 0.89f),
                            modifier = Modifier.size(24.dp).graphicsLayer {
                                scaleX = pressScale
                                scaleY = pressScale
                            },
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

/** Full-glass calendar shortcut displayed above the dock on Today only. */
@Composable
fun NavGlassCalendarButton(p: NavPalette, hazeState: HazeState, onClick: () -> Unit) {
    Box(
        Modifier.size(54.dp)
            .navFrostedOverlay(p, hazeState, radius = 27.dp, role = GlassRole.CONTROL)
            .glassClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NavGlassGlyph("calendar", p.blue, Modifier.size(26.dp))
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
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(42.dp)
            .navGlass(p, radius = 12.dp, role = GlassRole.CONTROL, elevation = 0.dp),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
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

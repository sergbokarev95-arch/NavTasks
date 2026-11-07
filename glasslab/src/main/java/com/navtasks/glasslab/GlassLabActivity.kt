package com.navtasks.glasslab

import android.app.Activity
import android.os.Bundle
import android.view.WindowInsetsController
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlin.math.abs

/**
 * Totally separate app ID; no NavTasks stores, profiles, network, permissions
 * or production UI state are accessible from this experimental laboratory.
 */
class GlassLabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GlassLabScreen() }
    }
}

@Composable
private fun GlassLabScreen() {
    var dark by remember { mutableStateOf(true) }
    var optical by remember { mutableStateOf(true) }
    var selected by remember { mutableIntStateOf(0) }
    val backdrop = rememberLayerBackdrop()
    val activity = LocalContext.current as? Activity
    SideEffect {
        // Follow the experimental Day/Night switch so system icons remain legible.
        val flags = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        activity?.window?.insetsController?.setSystemBarsAppearance(
            if (dark) 0 else flags, flags
        )
    }
    val bg = if (dark) Color(0xFF050A10) else Color(0xFFF3F6FB)
    val text = if (dark) Color(0xFFF2F5FA) else Color(0xFF142233)
    val muted = if (dark) Color(0xFFA9B6C7) else Color(0xFF627287)
    val accent = if (dark) Color(0xFF51B8FF) else Color(0xFF157DC7)

    Box(Modifier.fillMaxSize().background(bg)) {
        // RECORD THE SCENE (not an already blurred Haze surface).
        // The glass below is a sibling and must never capture itself.
        Box(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding()
                    .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(21.dp))
                Text("NAVTASKS  /  GLASS LAB", color = accent, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Text("Liquid Glass", color = text, fontSize = 32.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Реальный захват фона • динамическое преломление • без данных NavTasks",
                    color = muted, fontSize = 13.sp, lineHeight = 19.sp
                )
                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabChip(
                        label = if (dark) "Ночная тема" else "Дневная тема",
                        dark = dark, selected = true, onClick = { dark = !dark },
                        modifier = Modifier.weight(1f)
                    )
                    LabChip(
                        label = if (optical) "Стекло: ВКЛ" else "Стекло: ВЫКЛ",
                        dark = dark, selected = optical, onClick = { optical = !optical },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text("Прокручивай карточки под доком", color = muted, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                val items = listOf(
                    "Navigation" to "Проверка мостика и навигационного оборудования",
                    "X-band radar" to "Свечение и преломление текста за стеклянным доком",
                    "Compass Error" to "Рабочие записи и корректура компасов",
                    "GMDSS Records" to "Ежедневные проверки, журналы и сообщения",
                    "Sea Log" to "Скорость, погода, позиция и суточный переход",
                    "Services" to "Обслуживание оборудования и заявки AMOS",
                    "Handover" to "Записи для передачи дел следующему офицеру",
                    "Daily Reports" to "Сравнение обычного blur с настоящей линзой",
                    "Radar diagnostics" to "Текст должен двигаться ровно, без мерцания",
                    "Navigation Plan" to "Стабильный рендеринг при прокрутке и тапах",
                    "Ship safety" to "Пример разноцветных карточек для оптического теста"
                )
                items.forEachIndexed { idx, item ->
                    LabCard(
                        title = item.first,
                        subtitle = item.second,
                        index = idx,
                        dark = dark
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Spacer(Modifier.height(180.dp))
            }
        }

        GlassLabDock(
            backdrop = backdrop,
            selected = selected,
            onSelected = { selected = it },
            dark = dark,
            optical = optical,
            modifier = Modifier.align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 15.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun LabChip(
    label: String,
    dark: Boolean,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier.clip(shape)
            .background(
                if (selected) Color(0xFF217CB4).copy(alpha = if (dark) 0.35f else 0.13f)
                else if (dark) Color(0xFF142332) else Color.White
            )
            .border(1.dp, if (dark) Color(0xFF3C5266) else Color(0xFFC5D4E0), shape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 13.sp, color = if (dark) Color.White else Color(0xFF1D3651))
    }
}

@Composable
private fun LabCard(title: String, subtitle: String, index: Int, dark: Boolean) {
    val shape = RoundedCornerShape(21.dp)
    val edge = if (dark) Color(0xFF51677C) else Color(0xFFBCD0E4)
    val start = when (index % 4) {
        0 -> if (dark) Color(0xFF0C273B) else Color(0xFFE2F4FF)
        1 -> if (dark) Color(0xFF1D273E) else Color(0xFFF3E8FA)
        2 -> if (dark) Color(0xFF17352F) else Color(0xFFE5F5EE)
        else -> if (dark) Color(0xFF342739) else Color(0xFFFFEDF0)
    }
    val end = if (dark) Color(0xFF101D29) else Color(0xFFFFFFFF)
    Row(
        Modifier.fillMaxWidth().height(106.dp)
            .clip(shape).background(Brush.horizontalGradient(listOf(start, end)))
            .border(1.dp, edge, shape).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(33.dp).border(
                2.dp,
                if (dark) Color(0xFF55B7EE) else Color(0xFF277FAF),
                RoundedCornerShape(50)
            )
        )
        Spacer(Modifier.width(15.dp))
        Column {
            Text(
                title, color = if (dark) Color.White else Color(0xFF15293C),
                fontSize = 17.sp, fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                color = if (dark) Color(0xFFB8C5D3) else Color(0xFF50657A),
                fontSize = 12.sp, lineHeight = 17.sp
            )
        }
    }
}

/**
 * NavTasks-like outer geometry: full 56dp body, 15dp page insets, 4 slots.
 * No Haze, no nested blur of an already blurred texture. Backdrop records
 * the source scene and samples it in window coordinates before AGSL lensing.
 */
@Composable
private fun GlassLabDock(
    backdrop: Backdrop,
    selected: Int,
    onSelected: (Int) -> Unit,
    dark: Boolean,
    optical: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val ambient = if (dark) Color(0xFF111E2E).copy(alpha = .15f)
        else Color.White.copy(alpha = .12f)
    // Exact production NavTasks active blue, inactive icon and caption colors.
    val activeBlue = if (dark) Color(0xFF329CFF) else Color(0xFF087DFA)
    val inactiveGlyph = if (dark) Color(0xFFF2F8FF).copy(alpha = .89f)
        else Color(0xFF172B42).copy(alpha = .89f)
    val inactiveLabel = if (dark) Color(0xFFAFC4D8) else Color(0xFF5F768D)
    val fallback = if (dark) Color(0xDC1D2A38) else Color(0xEAE4EDF3)
    val surfaceSheen = Brush.verticalGradient(
        if (dark) listOf(
            Color.White.copy(alpha = .08f),
            Color(0xFF9FCBED).copy(alpha = .025f),
            Color.Black.copy(alpha = .045f)
        ) else listOf(
            Color.White.copy(alpha = .12f),
            Color.White.copy(alpha = .035f),
            Color(0xFF8CACCB).copy(alpha = .035f)
        )
    )

    // Keep the proven 0.1.1 direct-scene lens for the entire 56dp navigation bar.
    // Selection, glyphs and interaction remain on top and outside this shader.
    val backing = if (optical) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { Capsule() },
            effects = {
                vibrancy()
                blur(with(density) { GlassLabOptics.bar.blurDp.dp.toPx() })
                lens(
                    with(density) { GlassLabOptics.bar.refractionHeightDp.dp.toPx() },
                    with(density) { GlassLabOptics.bar.refractionAmountDp.dp.toPx() }
                )
            },
            highlight = { Highlight.Default.copy(alpha = .85f) },
            shadow = { Shadow(alpha = .22f) },
            onDrawSurface = {
                drawRect(ambient)
                drawRect(surfaceSheen)
            }
        )
    } else {
        Modifier.clip(RoundedCornerShape(30.dp)).background(fallback)
    }

    val opticalEdge = if (optical) {
        Modifier.border(
            width = .75.dp,
            brush = Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = if (dark) .36f else .49f),
                    Color(0xFF8FAFCB).copy(alpha = .12f),
                    Color.White.copy(alpha = if (dark) .17f else .28f)
                )
            ),
            shape = RoundedCornerShape(30.dp)
        )
    } else Modifier

    // Crucial: the refractive bar must NOT be a drawing modifier on the
    // parent of the moving lens. Its own clipping graphics layer would mask
    // the top and bottom protrusion, even with unbounded lens measurement.
    // A separate background sibling keeps backdrop capture and rim unchanged.
    BoxWithConstraints(modifier.fillMaxWidth().height(56.dp)) {
        Box(Modifier.matchParentSize().then(backing).then(opticalEdge))
        val cellWidth = maxWidth / GlassLabDockTabs.items.size
        val target = selected.coerceIn(0, GlassLabDockTabs.items.lastIndex).toFloat()
        // Follow the working NavTasks 0.4.59 movement: quick, interruptible spring,
        // while extra width and material settle independently of travel.
        val slide by animateFloatAsState(
            targetValue = target,
            animationSpec = spring(
                dampingRatio = 1f,
                stiffness = 550f,
                visibilityThreshold = .001f
            ),
            label = "glassLabDockSlide"
        )
        val moving = abs(slide - target) > .015f
        val matte by animateFloatAsState(
            targetValue = if (moving) 0f else 1f,
            animationSpec = spring(dampingRatio = 1f, stiffness = 700f),
            label = "glassLabLensMatte"
        )
        val stretched by animateFloatAsState(
            targetValue = if (moving) 1f else 0f,
            animationSpec = spring(dampingRatio = 1f, stiffness = 600f),
            label = "glassLabLensStretch"
        )
        // True proportional growth: both axes expand by the same factor.
        // Unlike 0.1.3's horizontal stretch, this makes the glass rise above
        // and drop below the 56dp navigation capsule while it is travelling.
        val lensScale = GlassLabDockTabs.scaleAt(stretched)
        val lensWidth = (cellWidth - 4.dp) * lensScale
        val lensHeight = GlassLabDockTabs.restingLensHeightDp.dp * lensScale
        val lensX = (cellWidth * (slide + .5f) - lensWidth / 2f)
            .coerceIn(2.dp, (maxWidth - 2.dp - lensWidth).coerceAtLeast(2.dp))
        val lensY = (GlassLabDockTabs.dockHeightDp.dp - lensHeight) / 2f

        // Selection is a sibling of the opaque/refractive BACKING, not its child.
        // The outer BoxWithConstraints has no clip() / graphicsLayer clip; the
        // lens draws in true window coordinates beyond the 56dp bar.
        // Exactly one moving lens. While travelling it is optically refractive;
        // after it lands, its own face fades into neutral graphite/grey.
        val selection = if (optical) {
            Modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(with(density) { GlassLabOptics.selection.blurDp.dp.toPx() })
                    lens(
                        with(density) { GlassLabOptics.selection.refractionHeightDp.dp.toPx() },
                        with(density) { GlassLabOptics.selection.refractionAmountDp.dp.toPx() }
                    )
                },
                highlight = { Highlight.Default.copy(alpha = .3f + (1f - matte) * .45f) },
                innerShadow = { InnerShadow(radius = 8.dp, alpha = .18f) },
                onDrawSurface = {
                    val traveling = if (dark) Color(0xFF75B8F0).copy(alpha = .13f)
                        else Color.White.copy(alpha = .19f)
                    val resting = if (dark) Color(0xFF616B77).copy(alpha = .86f)
                        else Color(0xFFD7D8DA).copy(alpha = .87f)
                    drawRect(lerp(traveling, resting, matte.coerceIn(0f, 1f)))
                }
            )
        } else {
            Modifier.clip(RoundedCornerShape(26.dp))
                .background(if (dark) Color(0xFF3C5368) else Color(0xFFBDD6E7))
        }

        Box(
            Modifier.offset(x = lensX, y = lensY)
                // Explicitly allow the travelling material to render outside
                // the 56dp dock bounds; otherwise Compose constraints can
                // coerce a taller lens back to the bar's own height.
                .wrapContentSize(unbounded = true, align = Alignment.TopStart)
                .size(width = lensWidth, height = lensHeight)
                .then(selection)
        )

        val view = LocalView.current
        Row(
            Modifier.fillMaxSize().padding(vertical = 2.dp).selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassLabDockTabs.items.forEachIndexed { index, item ->
                val active = selected == index
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val pressScale by animateFloatAsState(
                    targetValue = if (pressed) .94f else 1f,
                    animationSpec = spring(dampingRatio = 1f, stiffness = 1600f),
                    label = "glassLabTabPress"
                )
                Column(
                    Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(21.dp))
                        .selectable(
                            selected = active,
                            role = Role.Tab,
                            interactionSource = interaction,
                            indication = null
                        ) {
                            if (!active) {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                onSelected(index)
                            }
                        },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlassLabDockGlyph(
                        kind = item.glyph,
                        color = if (active) activeBlue else inactiveGlyph,
                        modifier = Modifier.size(24.dp).graphicsLayer {
                            scaleX = pressScale
                            scaleY = pressScale
                        }
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = item.label,
                        color = if (active) activeBlue else inactiveLabel,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

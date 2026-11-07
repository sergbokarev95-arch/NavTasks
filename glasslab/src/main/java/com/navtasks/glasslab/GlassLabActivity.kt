package com.navtasks.glasslab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
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
    val bg = if (dark) Color(0xFF050A10) else Color(0xFFF3F6FB)
    val text = if (dark) Color(0xFFF2F5FA) else Color(0xFF142233)
    val muted = if (dark) Color(0xFFA9B6C7) else Color(0xFF627287)
    val accent = if (dark) Color(0xFF51B8FF) else Color(0xFF157DC7)

    Box(Modifier.fillMaxSize().background(bg)) {
        // RECORD THE SCENE (not an already blurred Haze surface).
        // The glass below is a sibling and must never capture itself.
        Box(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .statusBarsPadding().padding(horizontal = 20.dp)
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
    val textColor = if (dark) Color(0xFFE3EBF4) else Color(0xFF142B3F)
    val blue = if (dark) Color(0xFF4BABFF) else Color(0xFF057DD2)
    val fallback = if (dark) Color(0xDC1D2A38) else Color(0xEAE4EDF3)

    val backing = if (optical) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { Capsule() },
            effects = {
                vibrancy()
                blur(with(density) { 8.dp.toPx() })
                lens(with(density) { 24.dp.toPx() }, with(density) { 24.dp.toPx() })
            },
            highlight = { Highlight.Default.copy(alpha = .8f) },
            shadow = { Shadow(alpha = .22f) },
            onDrawSurface = { drawRect(ambient) }
        )
    } else {
        Modifier.clip(RoundedCornerShape(30.dp)).background(fallback)
    }

    BoxWithConstraints(modifier.fillMaxWidth().height(56.dp).then(backing)) {
        val cellWidth = maxWidth / 4
        val offset by animateDpAsState(
            targetValue = cellWidth * selected,
            animationSpec = spring(dampingRatio = 1f, stiffness = 550f),
            label = "glassLabSelected"
        )
        val highlightModifier = if (optical) {
            Modifier.drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(with(density) { 4.dp.toPx() })
                    lens(with(density) { 18.dp.toPx() }, with(density) { 12.dp.toPx() })
                },
                highlight = { Highlight.Default.copy(alpha = .75f) },
                innerShadow = { InnerShadow(radius = 8.dp, alpha = .18f) },
                onDrawSurface = {
                    drawRect(
                        if (dark) Color(0xFF75B8F0).copy(alpha = .13f)
                        else Color.White.copy(alpha = .19f)
                    )
                }
            )
        } else Modifier.clip(RoundedCornerShape(26.dp))
            .background(if (dark) Color(0xFF3C5368) else Color(0xFFBDD6E7))

        Box(
            Modifier.offset(x = offset + 2.dp, y = 2.dp)
                .width(cellWidth - 4.dp).height(52.dp)
                .then(highlightModifier)
        )

        val symbols = listOf("◎", "▣", "⇥", "☷")
        val labels = listOf("Сегодня", "Работы", "Handover", "Задачи")
        Row(
            Modifier.fillMaxSize().padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { i ->
                Column(
                    Modifier.weight(1f).fillMaxHeight()
                        .clickable { onSelected(i) },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        symbols[i], fontSize = 23.sp,
                        color = if (selected == i) blue else textColor
                    )
                    Text(
                        labels[i], fontSize = 10.sp,
                        color = if (selected == i) blue else textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

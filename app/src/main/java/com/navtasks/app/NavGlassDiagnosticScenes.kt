package com.navtasks.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Only the captured content changes. The optical dock and Backdrop are identical. */
internal object NavGlassDiagnosticModes {
    const val LIVE_HAZE = 0
    const val LIVE_NO_HAZE = 1
    const val LAB_REFERENCE = 2
    val modes = listOf(
        LIVE_HAZE to "ЭКРАН",
        LIVE_NO_HAZE to "БЕЗ HAZE",
        LAB_REFERENCE to "ЭТАЛОН"
    )
}

@Composable
internal fun NavGlassDiagnosticControl(
    selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier.clip(shape).background(Color(0xF0192533))
            .border(1.dp, Color(0xFF5D7992), shape).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavGlassDiagnosticModes.modes.forEach { (mode, label) ->
            Text(
                label,
                color = if (selected == mode) Color.White else Color(0xFFB0C1D1),
                fontSize = 10.sp,
                modifier = Modifier.clip(RoundedCornerShape(17.dp))
                    .background(if (selected == mode) Color(0xFF27638E) else Color.Transparent)
                    .clickable { onSelect(mode) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

/** Original Glass Lab 0.1.5 card material, sizes, typography and card texts. */
@Composable
internal fun NavGlassDiagnosticReferenceScene(dark: Boolean) {
    val bg = if (dark) Color(0xFF050A10) else Color(0xFFF3F6FB)
    val text = if (dark) Color(0xFFF2F5FA) else Color(0xFF142233)
    val muted = if (dark) Color(0xFFA9B6C7) else Color(0xFF627287)
    val accent = if (dark) Color(0xFF51B8FF) else Color(0xFF157DC7)
    Box(Modifier.fillMaxSize().background(bg)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
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
            items.forEachIndexed { index, item ->
                NavGlassDiagnosticReferenceCard(item.first, item.second, index, dark)
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(180.dp))
        }
    }
}

/** Card body and numerical constants copied from the accepted LabCard. */
@Composable
private fun NavGlassDiagnosticReferenceCard(
    title: String, subtitle: String, index: Int, dark: Boolean
) {
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
                2.dp, if (dark) Color(0xFF55B7EE) else Color(0xFF277FAF),
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
                subtitle, color = if (dark) Color(0xFFB8C5D3) else Color(0xFF50657A),
                fontSize = 12.sp, lineHeight = 17.sp
            )
        }
    }
}

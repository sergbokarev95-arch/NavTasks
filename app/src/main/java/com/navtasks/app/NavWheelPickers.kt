package com.navtasks.app

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * All wheels sample Android's system-managed haptic settings.
 * Neither VIBRATE permission nor force-haptic flags are needed.
 */
@Composable
private fun Wheel(
    values: List<Int>,
    selected: Int,
    label: (Int) -> String,
    p: NavPalette,
    modifier: Modifier = Modifier,
    onChange: (Int) -> Unit,
) {
    val view = LocalView.current
    val currentOnChange by rememberUpdatedState(onChange)
    val currentSelected by rememberUpdatedState(selected)
    key(values.firstOrNull(), values.lastOrNull(), values.size) {
        val state = rememberLazyListState(
            initialFirstVisibleItemIndex = values.indexOf(selected).coerceAtLeast(0),
        )
        val scope = rememberCoroutineScope()
        val fling = rememberSnapFlingBehavior(lazyListState = state)

        LaunchedEffect(state, values) {
            var initial = true
            snapshotFlow { state.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collect { index ->
                    val n = values.getOrNull(index) ?: return@collect
                    if (!initial && n != currentSelected) {
                        currentOnChange(n)
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }
                    initial = false
                }
        }
        Box(modifier.height(240.dp), contentAlignment = Alignment.Center) {
            val indicator = RoundedCornerShape(12.dp)
            Box(
                Modifier.fillMaxWidth().height(48.dp)
                    .background(
                        NavTodayVisualTokens.checklistInsetSurface(p.dark),
                        indicator,
                    )
                    .border(1.dp, NavTodayVisualTokens.checklistInsetRim(p.dark), indicator),
            )
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 96.dp),
                flingBehavior = fling,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                itemsIndexed(values, key = { _, n -> n }) { index, n ->
                    Box(
                        Modifier.fillMaxWidth().height(48.dp)
                            .clickable(interactionSource = null, indication = null) {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                scope.launch { state.animateScrollToItem(index) }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            label(n),
                            color = if (n == selected) p.text else p.muted.copy(alpha = 0.68f),
                            fontSize = if (n == selected) 22.sp else 17.sp,
                            fontWeight = if (n == selected) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WheelDialogShell(
    title: String,
    p: NavPalette,
    lang: UiLanguage,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().personalNoteCard(p, 26.dp)
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, color = p.text, fontSize = 20.sp)
            content()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    Modifier.personalNoteCard(p, 12.dp, nested = true)
                        .glassClickable(onClick = onDismiss)
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                ) {
                    Text(localized("Отмена", "Cancel", lang), color = p.text, fontSize = 13.sp)
                }
                Spacer(Modifier.width(9.dp))
                Box(
                    Modifier.personalNoteCard(p, 12.dp, nested = true)
                        .glassClickable(onClick = onConfirm)
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                ) {
                    Text(localized("Выбрать", "Select", lang), color = NavTodayVisualTokens.accentInk(p.dark), fontSize = 13.sp)
                }
            }
        }
    }
}

/** Month/day rollover guard. */
internal fun wheelDate(year: Int, month: Int, day: Int): LocalDate =
    LocalDate.of(year, month, day.coerceIn(1, YearMonth.of(year, month).lengthOfMonth()))

@Composable
fun NavDateWheelDialog(
    initial: LocalDate,
    p: NavPalette,
    lang: UiLanguage,
    onDismiss: () -> Unit,
    onSelected: (LocalDate) -> Unit,
) {
    var day by remember(initial) { mutableIntStateOf(initial.dayOfMonth) }
    var month by remember(initial) { mutableIntStateOf(initial.monthValue) }
    var year by remember(initial) { mutableIntStateOf(initial.year.coerceIn(1900, 2200)) }
    val days = remember(year, month) { YearMonth.of(year, month).lengthOfMonth() }
    val locale = remember(lang) { if (lang == UiLanguage.RU) Locale("ru") else Locale.ENGLISH }
    WheelDialogShell(
        title = localized("Выбор даты", "Select date", lang),
        p = p, lang = lang, onDismiss = onDismiss,
        onConfirm = { onSelected(wheelDate(year, month, day)) },
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Wheel((1..days).toList(), day, { "%02d".format(it) }, p,
                Modifier.weight(0.8f)) { day = it }
            Wheel((1..12).toList(), month,
                { Month.of(it).getDisplayName(TextStyle.FULL_STANDALONE, locale) },
                p, Modifier.weight(1.15f)) {
                month = it
                day = day.coerceAtMost(YearMonth.of(year, it).lengthOfMonth())
            }
            Wheel((1900..2200).toList(), year, { it.toString() }, p,
                Modifier.weight(1.05f)) {
                year = it
                day = day.coerceAtMost(YearMonth.of(it, month).lengthOfMonth())
            }
        }
    }
}

@Composable
fun NavTimeWheelDialog(
    initial: LocalTime,
    p: NavPalette,
    lang: UiLanguage,
    onDismiss: () -> Unit,
    onSelected: (LocalTime) -> Unit,
) {
    var hour by remember(initial) { mutableIntStateOf(initial.hour) }
    var minute by remember(initial) { mutableIntStateOf(initial.minute) }
    WheelDialogShell(
        title = localized("Выбор времени", "Select time", lang),
        p = p, lang = lang, onDismiss = onDismiss,
        onConfirm = { onSelected(LocalTime.of(hour, minute)) },
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Wheel((0..23).toList(), hour, { "%02d".format(it) }, p,
                Modifier.weight(1f)) { hour = it }
            Text(":", color = p.text, fontSize = 27.sp)
            Wheel((0..59).toList(), minute, { "%02d".format(it) }, p,
                Modifier.weight(1f)) { minute = it }
        }
    }
}

package com.navtasks.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

/** Same renderer, geometry, animation and color as the Calendar; four existing frequencies. */
@Composable
internal fun NavTaskFrequencyControl(
    selected: Frequency,
    p: NavPalette,
    lang: UiLanguage,
    onSelect: (Frequency) -> Unit,
) {
    val frequencies = Frequency.entries
    val labels = navTaskFrequencyChoices(lang).map { it.second }
    val selectedIndex = frequencies.indexOf(selected).coerceAtLeast(0)
    NavCalendarSegmentedControl(labels, selectedIndex, p, Modifier.padding(vertical = 3.dp)) { i ->
        onSelect(frequencies[i])
    }
}

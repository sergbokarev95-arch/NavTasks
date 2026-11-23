package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** UI-only visual refresh: existing TaskStore keys, calendar and alert timings stay identical. */
class NavTaskChoiceDialogTest {
    @Test fun frequencySelectionPreservesEveryExistingTaskModelKey() {
        val list = navTaskFrequencyChoices(UiLanguage.RU)
        assertEquals(Frequency.entries.map { it.name }, list.map { it.first })
        assertEquals(listOf("День", "Неделя", "Месяц", "Разовая"),
            list.map { it.second })
        assertEquals(listOf("Daily", "Weekly", "Monthly", "One-off"),
            navTaskFrequencyChoices(UiLanguage.EN).map { it.second })
    }

    @Test fun repeatChoiceStillSelectsSameTwoCalculationModes() {
        assertEquals(
            listOf(ScheduleMode.CALENDAR.name, ScheduleMode.AFTER_COMPLETION.name),
            navTaskRepeatChoices(UiLanguage.RU).map { it.first },
        )
        assertEquals(listOf("По расписанию", "От выполнения"),
            navTaskRepeatChoices(UiLanguage.RU).map { it.second })
    }

    @Test fun reminderIntervalsStillMatchExistingMinutesAndLabels() {
        val choices = navTaskReminderChoices(UiLanguage.RU)
        assertEquals(listOf("0", "15", "60", "1440"), choices.map { it.first })
        assertEquals(listOf("В срок", "За 15 минут", "За час", "За день"),
            choices.map { it.second })
        assertEquals(listOf("At deadline", "15 minutes before",
            "1 hour before", "1 day before"),
            navTaskReminderChoices(UiLanguage.EN).map { it.second })
    }

    @Test fun editorPreservesExistingKeysAndPopupOnlyCommitsConfirmedDraft() {
        val ui = File("src/main/java/com/navtasks/app/NavTaskChoiceDialog.kt").readText()
        val editor = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        assertTrue(ui.contains("var draft by remember(selected, options)"))
        assertTrue(ui.contains("if (confirm) onSelect(draft) else onDismiss()"))
        assertTrue(ui.contains(".clickable(enabled = !closing) { close(false) }"))
        assertTrue(ui.contains(".clickable(enabled = !closing) { close(true) }"))
        assertTrue(ui.contains("Popup("))
        assertTrue(ui.contains("PopupPositionProvider"))
        assertTrue(ui.contains("anchorBounds.bottom + gapPx"))
        assertTrue(ui.contains("TransformOrigin(.5f, if (above) 1f else 0f)"))
        assertFalse(ui.contains("NavApplyDialogFrost("))
        assertFalse(ui.contains("navPopupGlassSurface("))
        assertTrue(ui.contains("Color(0xFF23272C)"))
        assertTrue(ui.contains("Color(0xFFF8F9FA)"))
        assertTrue(editor.contains("NavTaskFrequencyControl(frequency, p, lang)"))
        assertTrue(editor.contains("frequency = it"))
        assertTrue(editor.contains("mode = ScheduleMode.valueOf(key)"))
        assertTrue(editor.contains("reminder = key.toInt()"))
        assertTrue(editor.contains("remindMinutesBefore = reminder"))
        assertTrue(editor.contains("notificationsEnabled = notified"))
        assertFalse(editor.contains("openTaskChoice"))
        assertFalse(ui.contains("TaskStore("))
        assertFalse(ui.contains("NavNotifications."))
    }

    @Test fun fourFrequencySegmentsReuseAcceptedCalendarVisualTokensWithoutChangingModel() {
        val segmented = File("src/main/java/com/navtasks/app/NavTaskFrequencyControl.kt").readText()
        assertTrue(segmented.contains("val frequencies = Frequency.entries"))
        assertTrue(segmented.contains("navCalendarModeHeight"))
        assertTrue(segmented.contains("navCalendarModeSurface(p)"))
        assertTrue(segmented.contains("navCalendarLensResting(p.dark)"))
        assertTrue(segmented.contains("navCalendarRim(p.dark)"))
        assertTrue(segmented.contains("onSelect(frequencies[i])"))
        assertFalse(segmented.contains("TaskStore("))
    }

    @Test fun existingTaskCalendarAndNotificationModulesStayUnmodified() {
        val ui = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        assertTrue(ui.contains("TaskSchedule.effectiveFrequency(it, LocalDate.now())"))
        assertTrue(ui.contains("pendingFrequencyEdit = edited to preview"))
        assertTrue(ui.contains("calendarDayEditPreview(it, edited, store.runs"))
        assertTrue(ui.contains("preserveHistoricalCalendarBase(it, edited, LocalDate.now())"))
        assertTrue(ui.contains("if (frequency != Frequency.EXTRA && mode == ScheduleMode.AFTER_COMPLETION)"))
        assertTrue(ui.contains("SmartReminderTiming").not()) // timing belongs to notifications, not this UI.
    }
}

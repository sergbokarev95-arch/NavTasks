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
        assertTrue(ui.contains("NavTodayVisualTokens.cardSurface(p.dark)"))
        assertTrue(ui.contains("NavTodayVisualTokens.checklistInsetSurface(p.dark)"))
        assertTrue(editor.contains("navEventRepeatChoices(lang)"))
        assertTrue(editor.contains("frequency = chosen"))
        assertTrue(editor.contains("mode = ScheduleMode.valueOf(it)"))
        assertTrue(editor.contains("key.toIntOrNull()?.let { addReminder(it) }"))
        assertTrue(editor.contains("remindMinutesBefore = reminder"))
        assertTrue(editor.contains("notificationsEnabled = notified"))
        assertFalse(editor.contains("openTaskChoice"))
        assertFalse(ui.contains("TaskStore("))
        assertFalse(ui.contains("NavNotifications."))
    }

    @Test fun fourFrequencySegmentsReuseAcceptedCalendarVisualTokensWithoutChangingModel() {
        val segmented = File("src/main/java/com/navtasks/app/NavTaskFrequencyControl.kt").readText()
        assertTrue(segmented.contains("val frequencies = Frequency.entries"))
        assertTrue(segmented.contains("NavCalendarSegmentedControl(labels, selectedIndex, p"))
        val calendar = File("src/main/java/com/navtasks/app/NavCalendarModeControl.kt").readText()
        assertTrue(calendar.contains("navCalendarModeHeight"))
        assertTrue(calendar.contains("navCalendarModeLensHeight"))
        assertTrue(calendar.contains("NavLiquidDockMotion.peakScaleForTap"))
        assertTrue(calendar.contains("NavLiquidDockTabs.activeInk(dark)"))
        assertTrue(calendar.contains("navLiquidGlassRim(dark)"))
        assertTrue(segmented.contains("onSelect(frequencies[i])"))
        assertFalse(segmented.contains("TaskStore("))
    }

    @Test fun eventNamePrecedesRepeatAndEditorUsesBottomUpMotion() {
        val editor = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        val root = File("src/main/java/com/navtasks/app/NavTasksUi.kt").readText()
        val choice = File("src/main/java/com/navtasks/app/NavTaskChoiceDialog.kt").readText()
        val section = editor.substringAfter(
            "Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)"
        )
        val taskName = section.indexOf("NavEventTitle(state.title")
        val repeat = section.indexOf("state.EditorSchedule(p, lang)")
        assertTrue(taskName >= 0 && repeat > taskName)
        assertFalse(section.substringBefore("fun SettingsScreen(").contains("NavTaskFrequencyControl(frequency"))
        assertTrue(editor.contains("sheetMode: Boolean = false"))
        assertTrue(root.contains("label = \"taskEditorBottomSheet\""))
        assertTrue(root.contains("label = \"taskEditorScrim\""))
        assertTrue(root.contains("sheetMode = true"))
        assertTrue(root.contains("BackHandler(enabled = editorOpen)"))
        assertTrue(root.contains("onSave = { store.upsert(it); editorOpen = false }"))
        assertTrue(choice.contains("scaleY = .62f + .38f * progress"))
        assertTrue(choice.contains("TransformOrigin(.5f, if (above) 1f else 0f)"))
        assertFalse(choice.contains("NavApplyDialogFrost("))
        assertFalse(root.contains("if (editorOpen) {\n                TaskEditorScreen("))
    }

    @Test fun sheetDragDismissesWithoutLargeBackButtonOrOverlappingPopupBlur() {
        val editor = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        val root = File("src/main/java/com/navtasks/app/NavTasksUi.kt").readText()
        val choice = File("src/main/java/com/navtasks/app/NavTaskChoiceDialog.kt").readText()

        // Keep the existing Android Back gesture and Save; the big round
        // editor back control is reserved for the legacy full-screen mode.
        assertTrue(editor.contains("if (!sheetMode) {"))
        assertTrue(editor.contains("if (!sheetMode && editorBackdrop != null)"))
        assertTrue(editor.contains("sheetDragState: DraggableState? = null"))
        assertTrue(editor.contains("onSheetDragStopped: (Float) -> Unit"))
        assertTrue(root.contains("val editorSheetDragState = rememberDraggableState"))
        assertTrue(root.contains("editorSheetDragPx >= dismissSheetDistance"))
        assertTrue(root.contains("sheetDragState = editorSheetDragState"))
        assertTrue(root.contains("onSheetDragStopped = onEditorSheetDragStopped"))
        assertTrue(root.contains("BackHandler(enabled = editorOpen)"))
        assertTrue(root.contains("store.upsert(it); editorOpen = false"))

        // Underlying destination and the editor are separate surfaces: each
        // blurred once, opaque Popup drawn ABOVE the editor's light scrim.
        assertTrue(root.contains("editorOpen -> 4.dp"))
        assertTrue(root.contains("alpha = 0.36f"))
        assertTrue(editor.contains("if (showRepeat) NavEventChoiceDialog("))
        assertTrue(editor.contains("if (showReminder) NavEventChoiceDialog("))
        assertTrue(choice.contains("onExpandedChange(true)"))
        assertTrue(choice.contains("onExpandedChange(false)"))
        assertFalse(choice.contains("NavApplyDialogFrost("))
        assertTrue(choice.contains("Popup("))
    }

    @Test fun taskEditorUsesHeaderCheckWithoutChangingPersistence() {
        val editor = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        val header = File("src/main/java/com/navtasks/app/NavEventEditorUi.kt").readText()
        assertTrue(editor.contains("NavEventHeaderAction("))
        assertTrue(editor.contains("\"check\", localized(\"Сохранить\", \"Save\", lang), p, valid"))
        assertTrue(header.contains("IconButton(onClick = onClick, enabled = enabled"))
        assertTrue(editor.contains("preserveHistoricalCalendarBase(it, edited, LocalDate.now())"))
        assertTrue(editor.contains("pendingFrequencyEdit = edited to preview"))
    }

    @Test fun raisedEditorSheetUsesReferenceGraphiteWithoutChangingMainCanvas() {
        assertEquals(androidx.compose.ui.graphics.Color(0xFF2C2C2C), navEditorSheetSurface(navPalette(true)))
        assertEquals(navPalette(false).background, navEditorSheetSurface(navPalette(false)))
        assertEquals(androidx.compose.ui.graphics.Color.Black, navPalette(true).background)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF202022), NavTodayVisualTokens.cardSurface(true))
        val root = File("src/main/java/com/navtasks/app/NavTasksUi.kt").readText()
        assertTrue(root.contains(".background(navEditorSheetSurface(palette), sheetShape)"))
    }

    @Test fun existingTaskCalendarAndNotificationModulesStayUnmodified() {
        val ui = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
        assertTrue(ui.contains("TaskSchedule.effectiveFrequency(it, LocalDate.now())"))
        assertTrue(ui.contains("pendingFrequencyEdit = edited to preview"))
        assertTrue(ui.contains("calendarDayEditPreview(it, edited, store.runs"))
        assertTrue(ui.contains("preserveHistoricalCalendarBase(it, edited, LocalDate.now())"))
        assertTrue(ui.contains("if (frequency != Frequency.EXTRA && mode == ScheduleMode.AFTER_COMPLETION)"))
        assertFalse(ui.contains("SmartReminderTiming.trigger(")) // alarm scheduling stays outside the UI.
    }
}

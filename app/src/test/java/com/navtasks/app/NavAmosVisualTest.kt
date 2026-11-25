package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NavAmosVisualTest {
    @Test fun amosListUsesNeutralSurfacesAndPreservesNavigation() {
        val ui = File("src/main/java/com/navtasks/app/WorkOrdersUi.kt").readText()
        assertFalse(ui.contains("NavBrandLabel(p)"))
        assertFalse(ui.contains("GlassRole.SELECTED"))
        assertFalse(ui.contains("p.blue"))
        assertTrue(ui.contains("neutralStyle = true"))
        assertTrue(ui.contains("NavCalendarSegmentedControl("))
        assertTrue(ui.contains("personalNoteCard(p, NavEventLayout.cardRadius)"))
        assertTrue(ui.contains("NavTodayVisualTokens.accentInk(p.dark)"))
        assertTrue(ui.contains("items(filteredOrders, key = { it.id })"))
        assertTrue(ui.contains("items(filteredClosed, key = { it.id })"))
        assertTrue(ui.contains("WorkOverviewFilter.OVERDUE"))
        assertTrue(ui.contains("workOrders.complete(order.id, completionDate, shiftFuture)"))
        assertTrue(ui.contains("workOrders.restore(order.id)"))
    }
    @Test fun amosPopupsAndSearchUseScopedNeutralTreatment() {
        val root = "src/main/java/com/navtasks/app/"
        val ui = File(root + "WorkOrdersUi.kt").readText()
        val glass = File(root + "NavGlass.kt").readText()
        val animated = File(root + "NavAnimatedSearchField.kt").readText()
        assertTrue(ui.contains("highContrastField = true"))
        assertTrue(glass.contains("neutralStyle && highContrastField && !p.dark"))
        assertTrue(animated.contains("highContrastField = highContrastField"))
        assertTrue(ui.contains("neutralActions = true, neutralSurface = true"))
        assertTrue(ui.contains("blur(if (modalOpen) 4.dp else 0.dp)"))
        assertTrue(ui.contains("NavEventChoiceDialog("))
        assertTrue(ui.contains("maxCharacters = 6000, neutralStyle = true"))
        assertTrue(ui.contains("workOrders.upsert(candidate)"))
        assertTrue(ui.contains("workOrders.deletePermanently(order.id)"))
    }
    @Test fun amosEditorUsesRoutineSheetAndPreservesSingleReminderAndRecurrence() {
        val ui = File("src/main/java/com/navtasks/app/WorkOrdersUi.kt").readText()
        assertTrue(ui.contains("amosEditorBottomSheet"))
        assertTrue(ui.contains("navEditorSheetSurface(p)"))
        assertTrue(ui.contains("BackHandler(enabled = editorOpen)"))
        assertTrue(ui.contains("NavEventHeaderAction"))
        assertTrue(ui.contains("NavEventSchedulePanel(p)"))
        assertTrue(ui.contains("localized(\"Выполнено\", \"Completed\", lang)"))
        assertTrue(ui.contains("NavGlassGlyph(\"calendar\""))
        assertTrue(ui.contains("remindDaysBefore = remindDays"))
        assertTrue(ui.contains("executionHistory = existing?.executionHistory.orEmpty()"))
        assertTrue(ui.contains("existing.repeatMonths == repeatMonths) existing.dueDate"))
        assertTrue(navAmosRepeatOptions(UiLanguage.RU).map { it.first } ==
            listOf("0:0", "7:0", "0:1", "0:3", "0:6", "0:12", "0:18", "0:36", "0:60", "custom"))
        assertTrue(navAmosReminderOptions(UiLanguage.RU).map { it.first } ==
            listOf("0", "1", "3", "7", "14", "30", "90"))
    }
    @Test fun amosAccentsAreOptInAndCommentFieldKeepsRichTextState() {
        val root = "src/main/java/com/navtasks/app/"
        val ui = File(root + "WorkOrdersUi.kt").readText()
        val event = File(root + "NavEventEditorUi.kt").readText()
        val rich = File(root + "NavRichNoteUi.kt").readText()
        assertTrue(ui.contains("p, valid, accent = true"))
        assertTrue(ui.contains("valueAccent = repeatDays > 0 || repeatMonths > 0 || customInterval"))
        assertTrue(ui.contains("valueAccent = remindDays > 0"))
        assertTrue(ui.contains("amosCommentField = true"))
        assertTrue(event.contains("accent = false, onClick = onClick"))
        assertTrue(event.contains("valueAccent = false, onClick = onClick"))
        assertTrue(rich.contains("amosCommentField: Boolean = false"))
        assertTrue(rich.contains("nestedSurface = amosCommentField"))
        assertTrue(rich.contains("focusedIndicatorColor = Color.Transparent"))
        assertTrue(rich.contains("unfocusedIndicatorColor = Color.Transparent"))
        assertTrue(rich.contains("placeholder = fieldPlaceholder"))
        assertTrue(rich.contains("onChange(state.annotatedString.text, state.toHtml())"))
    }
}

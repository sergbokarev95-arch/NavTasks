package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NavRoutineVisualTest {
    @Test fun routineListReusesCalendarAndMatteTokens() {
        val ui = File("src/main/java/com/navtasks/app/PlanAndTasksUi.kt").readText()
            .substringAfter("fun TasksScreenV2(").substringBefore("private fun FormLabel(")
        assertTrue(ui.contains("NavCalendarSegmentedControl("))
        val header = ui.substringAfter("// Same inline section treatment as Today").substringBefore("// Keep the same lazy list")
        assertTrue(header.contains("filtered.size.toString()"))
        assertTrue(header.contains("height(1.dp).background(p.border"))
        assertFalse(header.contains("personalNoteCard"))
        assertTrue(ui.contains("personalNoteCard(p, NavEventLayout.cardRadius)"))
        assertTrue(ui.contains("Arrangement.spacedBy(NavEventLayout.sectionGap)"))
        assertFalse(ui.contains("p.blue"))
        assertFalse(ui.contains("p.warningText"))
        assertFalse(ui.contains(".navGlass("))
        assertTrue(ui.contains("TaskSchedule.effectiveFrequency(it, LocalDate.now()) == filter"))
        assertTrue(ui.contains("itemsIndexed(filtered, key = { _, item -> item.id })"))
        assertTrue(ui.contains("onEdit(task)"))
        assertTrue(ui.contains("TextDecoration.LineThrough"))
    }
    @Test fun routineHeaderAndFloatingActionsMatchCalendar() {
        val ui = File("src/main/java/com/navtasks/app/RoutinesModuleScreen.kt").readText()
        assertFalse(ui.contains("NavBrandLabel(p)"))
        assertTrue(ui.contains("NavLiquidGlassCircleButton(p, backdrop, \"back\", onBack)"))
        assertTrue(ui.contains("NavLiquidGlassAddButton(p, backdrop, onAdd)"))
        assertTrue(ui.contains("TasksScreenV2("))
    }
}

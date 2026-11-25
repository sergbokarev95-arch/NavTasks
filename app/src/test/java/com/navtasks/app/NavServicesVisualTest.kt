package com.navtasks.app
import org.junit.Assert.*
import org.junit.Test
import java.io.File
class NavServicesVisualTest {
    @Test fun servicesUseNeutralDesignAndKeepScheduleSemantics() {
        val ui = File("src/main/java/com/navtasks/app/ServicesUi.kt").readText()
        assertFalse(ui.contains("NavBrandLabel(p)"))
        assertFalse(ui.contains("GlassRole.SELECTED"))
        assertTrue(ui.contains("highContrastField = true"))
        assertTrue(ui.contains("serviceEditorBottomSheet"))
        assertTrue(ui.contains("NavEventChoiceDialog("))
        assertTrue(ui.contains("amosCommentField = true"))
        assertTrue(ui.contains("localized(\"Выполнено\", \"Completed\", lang)"))
        assertTrue(ui.contains("nextDate = if (unplanned) null else next"))
        assertTrue(ui.contains("remindMonths = if (unplanned) 0 else alert"))
        assertTrue(ui.contains("serviceHistory = existing?.serviceHistory.orEmpty()"))
        assertTrue(ui.contains("services.complete(entry.id, completionDate)"))
        assertTrue(ui.contains("currentServiceRequests(activeServices)"))
    }
}

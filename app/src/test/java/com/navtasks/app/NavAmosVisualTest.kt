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
}

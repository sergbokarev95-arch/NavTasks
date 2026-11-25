package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NavWorksHubVisualTest {
    @Test fun hubUsesSharedMatteCardsAndSemanticStatus() {
        val ui = File("src/main/java/com/navtasks/app/WorksHubUi.kt").readText()
        assertTrue(ui.contains("personalNoteCard(p, NavEventLayout.cardRadius)"))
        assertTrue(ui.contains("NavEventLayout.sectionGap"))
        assertTrue(ui.contains("NavTodayVisualTokens.accentInk(p.dark)"))
        assertFalse(ui.contains("NavInfoSectionBanner("))
        assertFalse(ui.contains("p.blue"))
        assertTrue(ui.contains("if (overdueAmos > 0) WorkHubTone.OVERDUE else WorkHubTone.NORMAL"))
        assertTrue(ui.contains("if (overdue > 0) WorkHubTone.OVERDUE else WorkHubTone.NORMAL"))
        assertTrue(ui.contains("if (approaching > 0) WorkHubTone.SOON else WorkHubTone.NORMAL"))
    }
    @Test fun preservesAllRoutesAndExistingCounterSources() {
        val ui = File("src/main/java/com/navtasks/app/WorksHubUi.kt").readText()
        for (route in listOf("onTasks", "onAmos", "onServices"))
            assertTrue(ui.contains("onClick = " + route))
        assertTrue(ui.contains("filterServiceOverview(activeServices, today, WorkOverviewFilter.OVERDUE)"))
        assertTrue(ui.contains("filterServiceOverview(activeServices, today, WorkOverviewFilter.SOON)"))
        assertTrue(ui.contains("it.closedAt == null && !it.completed"))
        assertTrue(ui.contains("listState.animateScrollToItem(0)"))
    }
}

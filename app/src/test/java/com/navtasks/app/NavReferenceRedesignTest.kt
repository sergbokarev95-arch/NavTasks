package com.navtasks.app
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class NavReferenceRedesignTest {
    @Test fun unfinishedInspectionLibrariesAreNotNavigable() {
        assertFalse(referenceLibraryRouteAvailable(NavReferenceSections.SIRE))
        assertFalse(referenceLibraryRouteAvailable(NavReferenceSections.VIQ7))
        assertFalse(referenceLibraryRouteAvailable(null))
        assertFalse(referenceLibraryRouteAvailable("unknown"))
        assertTrue(referenceLibraryRouteAvailable(NavReferenceSections.PERSONAL))
        val ui = File("src/main/java/com/navtasks/app/ReferenceHubUi.kt").readText()
        assertTrue(ui.contains("if (referenceLibraryRouteAvailable(manualSection))"))
        assertTrue(ui.contains("LaunchedEffect(manualSection)"))
        assertEquals(2, Regex("unavailable = true").findAll(ui).count())
        assertTrue(ui.contains("!unavailable && onClick != null"))
        assertTrue(ui.contains("referenceLibraryRouteAvailable(it.sectionId)"))
        assertTrue(ui.contains("onClick = { inspectionsOpen = true }"))
        assertTrue(ui.contains("manualSection = NavReferenceSections.PERSONAL"))
    }
    @Test fun hubUsesNeutralCardsAndCoralStatusWithoutChangingExchange() {
        val ui = File("src/main/java/com/navtasks/app/ReferenceHubUi.kt").readText()
        assertFalse(ui.contains("NavInfoSectionBanner("))
        assertTrue(ui.contains("neutralStyle = true, highContrastField = true"))
        assertTrue(ui.contains("NavEventDivider(p)"))
        assertTrue(ui.contains("personalNoteCard(p, NavEventLayout.cardRadius)"))
        assertTrue(ui.contains("if (unavailable || statusAccent) NavTodayVisualTokens.accentInk(p.dark)"))
        assertTrue(ui.contains("ReferenceEquipmentSettingsPanel(profileId = profileId"))
        assertTrue(ui.contains("equipmentOpen = true"))
    }
}

package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Pre-1.0 UI routing contract: move entry points, not data or exchange codecs.
 * Complements the existing Reference v2 checksum and import conflict tests.
 */
class NavFinalReferenceStructureTest {
    private fun source(file: String) =
        File("src/main/java/com/navtasks/app/$file.kt").readText()

    @Test fun globalSettingsContainsOnlyProfileAndSelectiveExchange() {
        val settings = source("PlanAndTasksUi")
        assertTrue(settings.contains("ProfileExchangePanel("))
        assertTrue(settings.contains("SelectiveExchangePanel("))
        assertFalse(settings.contains("ReferenceEquipmentSettingsPanel("))
        assertFalse(settings.contains("ReferenceCatalogBackupPanel("))
    }

    @Test fun referenceHasExactlyThreeTopLevelCategoryRoutes() {
        val hub = source("ReferenceHubUi")
        val inspection = hub.indexOf("item(key = \"reference-inspections\")")
        val equipment = hub.indexOf("item(key = \"reference-equipment\")")
        val personal = hub.indexOf("item(key = \"reference-personal\")")
        assertTrue(inspection >= 0 && equipment > inspection && personal > equipment)
        assertFalse(hub.contains("item(key = \"reference-sire\")"))
        assertFalse(hub.contains("item(key = \"reference-viq\")"))
        assertTrue(hub.contains("item(key = \"inspection-sire\")"))
        assertTrue(hub.contains("item(key = \"inspection-viq\")"))
        val exchange = hub.indexOf("item(key = \"reference-exchange\")")
        assertTrue("Reference import/export is below the last category", exchange > personal)
        assertTrue("Reference import/export uses the Export Handover centered dimensions",
            hub.contains("Modifier.widthIn(min = 174.dp).heightIn(min = 46.dp)"))
        assertTrue(hub.contains("manualSection = NavReferenceSections.SIRE"))
        assertTrue(hub.contains("manualSection = NavReferenceSections.VIQ7"))
    }

    @Test fun independentReferenceExchangeRetainsVerifiedPdfDocxTransfer() {
        val hub = source("ReferenceHubUi")
        val exchange = source("ReferenceEquipmentSettingsUi")
        assertTrue(hub.contains("ReferenceEquipmentSettingsPanel(profileId = profileId"))
        assertTrue(hub.contains("exchangeOpen = true"))
        assertTrue(hub.contains("equipmentStore.read()"))
        assertTrue(exchange.contains("ReferenceEquipmentPortableV2.prepareVerifiedExport("))
        assertTrue(exchange.contains("ReferenceEquipmentPortableV2.stage("))
        assertTrue(exchange.contains("restoreStagedEquipmentOriginals("))
        assertTrue(exchange.contains("ReferenceCatalogBackupPanel("))
    }

    @Test fun mainDockKeepsReferenceIndependentOfTaskAndWorks() {
        val main = source("NavTasksUi")
        assertTrue(main.contains("else -> ReferenceHubScreen("))
        assertTrue(main.contains("profileId = profiles.activeId"))
    }
}

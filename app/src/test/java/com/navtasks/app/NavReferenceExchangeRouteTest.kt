package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** Verify both equipment exchange entry points use the same self-contained v2 codec. */
class NavReferenceExchangeRouteTest {
    private fun source(name: String) = File("src/main/java/com/navtasks/app/$name.kt").readText()

    @Test fun bothEquipmentScreensExportFullV2PackageAndImportVerifiedOriginals() {
        for (screen in listOf("ReferenceEquipmentUi", "ReferenceEquipmentSettingsUi")) {
            val code = source(screen)
            assertTrue("$screen must export full package",
                code.contains("ReferenceEquipmentPortableV2.prepareVerifiedExport("))
            assertTrue("$screen must stage imports with SHA verification",
                code.contains("ReferenceEquipmentPortableV2.stage("))
            assertTrue("$screen must restore original PDF/DOCX",
                code.contains("restoreStagedEquipmentOriginals("))
            assertFalse("$screen must not emit old metadata-only v1",
                code.contains("ReferenceEquipmentExchange.encode("))
            assertFalse("$screen must not use v1-only decoder",
                code.contains("ReferenceEquipmentExchange.decode("))
        }
    }

    @Test fun legacyV1WithoutOriginalsCanNeverBeRepresentedAsCompleteImport() {
        val settings = source("ReferenceEquipmentSettingsUi")
        val equipment = source("ReferenceEquipmentUi")
        assertTrue(settings.contains("staged.legacyMissingAttachments"))
        assertTrue(equipment.contains("staged.legacyMissingAttachments"))
        assertTrue(settings.contains("Без PDF"))
        assertTrue(equipment.contains("Без PDF"))
        assertTrue(settings.contains("!staged.legacyMissingAttachments"))
        assertTrue(equipment.contains("!staged.legacyMissingAttachments"))
    }

    @Test fun handoverExportIsBelowSectionsNotBesideSearchAndDialogIsOpaque() {
        val handover = source("HandoverUi")
        val listStart = handover.indexOf("LazyColumn(")
        val sections = handover.indexOf("items(shownSections", listStart)
        val export = handover.indexOf("item(key = \"handover-export\")", sections)
        assertTrue("Export must appear after all sections inside LazyColumn",
            listStart >= 0 && sections > listStart && export > sections)
        assertEquals(1, Regex("onClick = \\{ exportOpen = true \\}")
            .findAll(handover).count())
        assertTrue(handover.contains("solidSurface = true"))
        assertTrue(handover.contains("Text(\"Word (.docx)\", color = Color.White)"))
        assertTrue(handover.contains("Modifier.fillMaxWidth()"))
        val dialogs = source("NavOverlayUi")
        assertTrue(dialogs.contains("solidSurface: Boolean = false"))
        assertTrue(dialogs.contains("!solidSurface && !neutralSurface && navPopupFrostAvailable()"))
        assertTrue(dialogs.contains("neutralSurface: Boolean = false"))
    }
}

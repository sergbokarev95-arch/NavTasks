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

    @Test fun handoverExportIsPinnedBesideSearchAndDialogIsOpaque() {
        val handover = source("HandoverUi")
        val listStart = handover.indexOf("LazyColumn(")
        val sections = handover.indexOf("items(shownSections", listStart)
        val export = handover.indexOf("HandoverExportAction(p, lang")
        assertTrue("Export must be accessible before scrolling the section list",
            export >= 0 && listStart > export && sections > listStart)
        assertFalse(handover.contains("item(key = \"handover-export\")"))
        assertTrue(handover.contains("solidSurface = true"))
        assertTrue(handover.contains("neutralSurface = true, neutralActions = true"))
        assertTrue(handover.contains("HandoverExportOption(\"Word (.docx)\", p)"))
        assertTrue(handover.contains("HandoverExportOption(\"PDF\", p)"))
        assertTrue(handover.contains("exportSnapshot = handover.sections.toList()"))
        assertTrue(handover.contains("Modifier.fillMaxWidth()"))
        val dialogs = source("NavOverlayUi")
        assertTrue(dialogs.contains("solidSurface: Boolean = false"))
        assertTrue(dialogs.contains("!solidSurface && !neutralSurface && navPopupFrostAvailable()"))
        assertTrue(dialogs.contains("neutralSurface: Boolean = false"))
    }
}

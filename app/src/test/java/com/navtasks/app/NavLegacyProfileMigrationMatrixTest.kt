package com.navtasks.app

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * Historical-export-schema fixtures, not personal backups.
 * The fixture field names were checked against the historical source exporters:
 * 0.3.1 (4a7cd30), 0.4.0 (46e030c), 0.4.44 (f6692f3).
 *
 * This tests parsing/import/export, NOT an actual Android in-place install or
 * recovery of private SharedPreferences on a physical device.
 */
class NavLegacyProfileMigrationMatrixTest {
    private fun raw(name: String): String =
        requireNotNull(javaClass.classLoader?.getResourceAsStream("legacy_profiles/$name")) {
            "Missing immutable legacy profile fixture: $name"
        }.bufferedReader(Charsets.UTF_8).use { it.readText() }

    private fun parse(name: String): ProfileSnapshot =
        ProfileTransfer.parse(raw(name))

    private fun reexport(name: String): ProfileSnapshot {
        val first = parse(name)
        val second = ProfileTransfer.parse(ProfileTransfer.export(first))
        assertEquals("Legacy fixture changed after importing and exporting: $name", first, second)
        assertEquals(first.profile.id, second.profile.id)
        assertEquals(first.lastVisit, second.lastVisit)
        return second
    }

    @Test fun vessel031PreservesCompletedAndTransferredTaskRunWithDatedHandover() {
        val old = reexport("v0_3_1_vessel.json")
        assertEquals("legacy-031", old.profile.id)
        assertEquals(NavProfileKind.VESSEL, old.profile.kind)
        assertEquals(listOf("gmdss-031"), old.templates.map { it.id })
        assertEquals(1, old.runs.size)
        val completed = old.runs.single()
        assertEquals("gmdss-031@2026-03-06", completed.key)
        assertTrue(completed.completed)
        assertEquals("2026-03-06T14:20:00Z", completed.completedAt)
        assertEquals(LocalDate.parse("2026-03-07"), completed.postponedTo)
        assertTrue("gmdss-031-check" in completed.checkedIds)
        assertEquals(1, old.sections.size)
        val handover = old.sections.single().entries.single()
        assertEquals("ECDIS license", handover.title)
        assertEquals("Renewal noted 2026", handover.notes)
        assertEquals(HandoverMark.COMPLETED, handover.mark)
        assertEquals(LocalDate.parse("2026-03-05"), handover.completedDate)
        assertEquals("", handover.notesHtml) // not supported in 0.3.1, must default safely
        assertTrue(old.services.isEmpty())
        assertTrue(old.workOrders.isEmpty())
    }

    @Test fun personal031RetainsOldNotesWithoutInventingVesselModules() {
        val old = reexport("v0_3_1_personal.json")
        assertEquals("personal", old.profile.id)
        assertEquals(NavProfileKind.PERSONAL, old.profile.kind)
        assertEquals("Remember documents", old.sections.single().entries.single().notes)
        assertEquals(HandoverMark.OPEN, old.sections.single().entries.single().mark)
        assertTrue(old.templates.isEmpty())
        assertTrue(old.runs.isEmpty())
        assertTrue(old.services.isEmpty())
        assertTrue(old.workOrders.isEmpty())
    }

    @Test fun vessel040PreservesFiveYearServiceAndChecklistJournal() {
        val old = reexport("v0_4_0_vessel.json")
        assertEquals("legacy-040", old.profile.id)
        assertEquals("Log result", old.templates.single().checklist.single().comment)
        assertEquals("Log result", old.runs.single().checklist.single().comment)
        val service = old.services.single()
        assertEquals("radar-040", service.id)
        assertEquals(LocalDate.parse("2024-01-15"), service.lastDate)
        assertEquals(LocalDate.parse("2029-01-15"), service.nextDate)
        assertEquals(60, service.periodMonths)
        assertEquals(3, service.remindMonths)
        assertEquals("Five year overhaul", service.comment)
        assertTrue(service.serviceHistory.isEmpty()) // introduced later
        assertTrue(old.workOrders.isEmpty()) // introduced later
    }

    @Test fun vessel044PreservesServiceRequestHistoryAndAmosExecutions() {
        val old = reexport("v0_4_44_vessel.json")
        assertEquals("legacy-044", old.profile.id)
        assertEquals("Check radio", old.runs.single().checklist.single().comment)
        val service = old.services.single()
        assertEquals("REQ-044", service.requestNumber)
        assertEquals(12, service.periodMonths)
        assertEquals("REQ-043", service.serviceHistory.single().requestNumber)
        assertEquals(LocalDate.parse("2025-01-11"),
            service.serviceHistory.single().completedAt)
        assertEquals("", service.commentHtml) // rich text added later
        val work = old.workOrders.single()
        assertEquals("amos-044", work.id)
        assertEquals("AMOS-044", work.reference)
        assertEquals(LocalDate.parse("2026-08-01"), work.dueDate)
        assertEquals(12, work.repeatMonths)
        assertEquals("Document gyro deviation", work.checklist.single().comment)
        assertEquals(LocalDate.parse("2025-08-02"),
            work.executionHistory.single().completedAt)
        assertTrue("amos-044-check" in work.executionHistory.single().checkedItemIds)
        assertEquals("", work.commentHtml)
    }

    @Test fun staleOldFilesAreRejectedBeforeAnyWriteWhenHistoryReferencesWrongTask() {
        val original = raw("v0_3_1_vessel.json")
        val invalid = JSONObject(original)
        val history = invalid.getJSONArray("history")
        history.getJSONObject(0).put("taskId", "missing-old-task")
        assertTrue(runCatching { ProfileTransfer.parse(invalid.toString()) }.isFailure)
        assertEquals("Source fixture must never be modified during failed import",
            original, raw("v0_3_1_vessel.json"))
    }

    @Test fun corruptOrUnsupportedLegacyExportsAreNotAcceptedSilently() {
        val source = JSONObject(raw("v0_4_44_vessel.json"))
        val unsupported = JSONObject(source.toString()).put("schemaVersion", 2)
        assertTrue(runCatching { ProfileTransfer.parse(unsupported.toString()) }.isFailure)
        val duplicate = JSONObject(source.toString())
        duplicate.getJSONArray("tasks").put(
            JSONObject(duplicate.getJSONArray("tasks").getJSONObject(0).toString()))
        assertTrue(runCatching { ProfileTransfer.parse(duplicate.toString()) }.isFailure)
        val lostSection = JSONObject(source.toString())
        lostSection.put("handover", JSONArray().put(
            JSONObject().put("id", "section-1").put("title", "")
                .put("entries", JSONArray())))
        assertTrue(runCatching { ProfileTransfer.parse(lostSection.toString()) }.isFailure)
    }

    @Test fun skipVersionsFollowPublishedFloorAndNeverImplyArbitraryMigration() {
        assertTrue(navUpdateSupportsDirectUpgrade(100, 200, 100))
        assertTrue(navUpdateSupportsDirectUpgrade(143, 200, 100))
        assertTrue(navUpdateSupportsDirectUpgrade(186, 200, 186))
        assertFalse(navUpdateSupportsDirectUpgrade(99, 200, 100))
        assertFalse(navUpdateSupportsDirectUpgrade(186, 200, 187))
        assertFalse(navUpdateSupportsDirectUpgrade(186, 185, 100))
    }

    @Test fun originalPreProfileMigrationCopiesButDoesNotDeleteLegacyPrefs() {
        val app = java.io.File("src/main/java/com/navtasks/app/ProfileStore.kt").readText()
        assertTrue(app.contains("getSharedPreferences(\"navtasks-v1\""))
        assertTrue(app.contains("getSharedPreferences(\"navtasks-handover-v1\""))
        assertTrue(app.contains("if (!shipData.contains(\"templates\")"))
        assertTrue(app.contains("if (!shipData.contains(\"sections\")"))
        assertTrue(app.contains("navChooseProfileIndex("))
        assertFalse(app.contains("original.edit().clear()"))
        assertFalse(app.contains("notes.edit().clear()"))
    }
}

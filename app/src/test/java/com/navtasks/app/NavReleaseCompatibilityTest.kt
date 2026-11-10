package com.navtasks.app

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Synthetic historic full-profile-v1 examples, not actual user backups.
 * Tests supported old optional-field behavior. Device in-place testing on
 * real archived profiles is still mandatory before a stable 1.0.0 release.
 */
class NavReleaseCompatibilityTest {
    private fun oldVesselExport(): JSONObject = JSONObject()
        .put("format", "navtasks-full-profile-v1")
        .put("schemaVersion", 1)
        .put("profile", JSONObject()
            .put("id", "vessel-legacy").put("title", "Ship").put("kind", "VESSEL"))
        .put("tasks", JSONArray().put(JSONObject()
            .put("id", "task-gmdss").put("title", "GMDSS Weekly")
            .put("frequency", "WEEKLY").put("startDate", "2024-01-05")))
        .put("history", JSONArray().put(JSONObject()
            .put("taskId", "task-gmdss").put("dueDate", "2024-01-12")
            .put("completed", true)))
        .put("handover", JSONArray().put(JSONObject()
            .put("id", "navigation").put("title", "Navigation")
            .put("entries", JSONArray().put(JSONObject()
                .put("id", "ecdis").put("title", "ECDIS")
                .put("notes", "Legacy chart update")))))
        .put("services", JSONArray().put(JSONObject()
            .put("id", "compass").put("equipment", "Compass")
            .put("lastDate", "2024-01-01").put("nextDate", "2024-07-01")
            .put("periodMonths", 6)))
        .put("lastVisit", "2024-01-12")
        // Old exports had no workOrders, HTML, unplanned or history snapshots.

    @Test fun preWorkOrdersFullProfileParsesAndPreservesCoreRecords() {
        val snapshot = ProfileTransfer.parse(oldVesselExport().toString())
        assertEquals(NavProfileKind.VESSEL, snapshot.profile.kind)
        assertEquals("task-gmdss", snapshot.templates.single().id)
        assertTrue(snapshot.runs.single().completed)
        assertEquals("Legacy chart update", snapshot.sections.single().entries.single().notes)
        assertEquals("", snapshot.sections.single().entries.single().notesHtml)
        assertEquals("Compass", snapshot.services.single().equipment)
        assertFalse(snapshot.services.single().unplanned)
        assertTrue(snapshot.workOrders.isEmpty())
        val roundTrip = ProfileTransfer.parse(ProfileTransfer.export(snapshot))
        assertEquals(snapshot.templates.single().id, roundTrip.templates.single().id)
        assertEquals(snapshot.runs.single().key, roundTrip.runs.single().key)
        assertEquals(snapshot.sections.single().entries.single().notes,
            roundTrip.sections.single().entries.single().notes)
        assertEquals(snapshot.services.single().id, roundTrip.services.single().id)
    }

    @Test fun legacyPersonalFullProfileCanRemainNotesOnly() {
        val json = JSONObject()
            .put("format", "navtasks-full-profile-v1").put("schemaVersion", 1)
            .put("profile", JSONObject().put("id", "personal")
                .put("title", "Личное").put("kind", "PERSONAL"))
            .put("tasks", JSONArray()).put("history", JSONArray())
            .put("handover", JSONArray().put(JSONObject()
                .put("id", "misc").put("title", "Разное")
                .put("entries", JSONArray().put(JSONObject()
                    .put("id", "note1").put("title", "Заметка 1")
                    .put("notes", "Текст заметки")))))
            .put("lastVisit", "2025-01-01")
        val snapshot = ProfileTransfer.parse(json.toString())
        assertTrue(snapshot.templates.isEmpty())
        assertTrue(snapshot.runs.isEmpty())
        assertTrue(snapshot.services.isEmpty())
        assertTrue(snapshot.workOrders.isEmpty())
        assertEquals("Текст заметки", snapshot.sections.single().entries.single().notes)
    }

    @Test fun unsupportedOrDanglingHistoricalDataFailsWithoutMutation() {
        val old = oldVesselExport()
        old.getJSONArray("history").put(JSONObject()
            .put("taskId", "missing-task").put("dueDate", "2024-02-01"))
        val originalRaw = old.toString()
        assertTrue(runCatching { ProfileTransfer.parse(originalRaw) }.isFailure)
        assertEquals(originalRaw, old.toString())
    }
}

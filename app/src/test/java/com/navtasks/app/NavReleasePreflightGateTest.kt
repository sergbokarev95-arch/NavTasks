package com.navtasks.app

import java.io.IOException
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

/** Synthetic QA fixtures only. Never read real user profiles or device files. */
class NavReleasePreflightGateTest {
    private val day = LocalDate.of(2026, 11, 12)

    @Test fun fullVesselExportRoundTripKeepsEveryModuleAndFormattedNotes() {
        val checklist = listOf(CheckItem(
            id = "check-radio", title = "GMDSS weekly test",
            comment = "Record result", commentHtml = "<p><b>Record</b> result</p>"))
        val task = TaskTemplate(
            id = "task-gmdss", title = "Weekly GMDSS",
            frequency = Frequency.WEEKLY, startDate = day.minusWeeks(3),
            checklist = checklist)
        val completion = TaskRun(task.id, day.minusDays(2), checklist,
            checkedIds = setOf("check-radio"), completed = true,
            completedAt = "2026-11-10T12:30:00Z")
        val section = HandoverSection(id = "bridge", title = "Navigation",
            entries = listOf(HandoverEntry(id = "ecdis", title = "ECDIS license",
                notes = "Renew in December",
                notesHtml = "<p><u>Renew</u> in December</p>",
                mark = HandoverMark.COMPLETED,
                completedDate = day.minusDays(1))))
        val service = ServiceEntry(
            id = "service-radar", equipment = "Radar magnetron",
            lastDate = day.minusMonths(3), nextDate = day.plusMonths(9),
            periodMonths = 12, remindMonths = 3, requestNumber = "REQ-002",
            comment = "Check antenna", commentHtml = "<p><i>Check</i> antenna</p>",
            serviceHistory = listOf(ServiceCompletion(
                scheduledDate = day.minusMonths(3), completedAt = day.minusMonths(3),
                requestNumber = "REQ-001", equipment = "Radar magnetron",
                comment = "Earlier service", intervalMonths = 12)))
        val amos = WorkOrderEntry(
            id = "amos-gyro", reference = "AMOS-124", title = "Gyro calibration",
            dueDate = day.plusMonths(1), createdAt = day.minusMonths(5),
            comment = "Verify heading", commentHtml = "<p><b>Verify</b> heading</p>")
        val profile = NavProfile("vessel-qa", "QA Vessel", NavProfileKind.VESSEL)
        val original = ProfileSnapshot(profile, listOf(task), listOf(completion),
            listOf(section), day.toString(), listOf(service), listOf(amos))

        val encoded = ProfileTransfer.export(original)
        val restored = ProfileTransfer.parse(encoded)
        val second = ProfileTransfer.parse(ProfileTransfer.export(restored))

        for (result in listOf(restored, second)) {
            assertEquals("vessel-qa", result.profile.id)
            assertEquals(original.lastVisit, result.lastVisit)
            assertEquals(task.id, result.templates.single().id)
            assertEquals(checklist.single().commentHtml,
                result.templates.single().checklist.single().commentHtml)
            assertEquals(completion.key, result.runs.single().key)
            assertTrue(result.runs.single().completed)
            assertEquals(section.entries.single().notesHtml,
                result.sections.single().entries.single().notesHtml)
            assertEquals(day.minusDays(1),
                result.sections.single().entries.single().completedDate)
            assertEquals(service.id, result.services.single().id)
            assertEquals("REQ-001", result.services.single().serviceHistory.single().requestNumber)
            assertEquals(service.commentHtml, result.services.single().commentHtml)
            assertEquals(amos.id, result.workOrders.single().id)
            assertEquals(amos.commentHtml, result.workOrders.single().commentHtml)
        }
    }

    @Test fun oldFullProfileFormatRejectsInvalidHistoryInsteadOfDroppingIt() {
        val task = TaskTemplate(id = "existing", title = "Daily inspection",
            frequency = Frequency.DAILY, startDate = day.minusDays(20))
        val good = ProfileSnapshot(
            NavProfile("old-vessel", "Older vessel", NavProfileKind.VESSEL),
            listOf(task), emptyList(), emptyList(), day.toString())
        val old = org.json.JSONObject(ProfileTransfer.export(good))
        old.remove("workOrders")
        old.remove("services")
        assertTrue(ProfileTransfer.parse(old.toString()).workOrders.isEmpty())
        assertTrue(ProfileTransfer.parse(old.toString()).services.isEmpty())
        val originalRaw = old.toString()
        val corrupt = org.json.JSONObject(originalRaw)
        corrupt.put("history", org.json.JSONArray().put(
            org.json.JSONObject().put("taskId", "deleted-task")
                .put("dueDate", day.toString()).put("completed", true)))
        assertTrue(runCatching { ProfileTransfer.parse(corrupt.toString()) }.isFailure)
        // A malformed candidate must not mutate the existing in-memory source.
        assertEquals(originalRaw, old.toString())
    }

    @Test fun skippedVersionsRequireCompatibleFloorAndNeverDowngrade() {
        assertTrue(navUpdateSupportsDirectUpgrade(81, 250, 81))
        assertTrue(navUpdateSupportsDirectUpgrade(143, 250, 143))
        assertTrue(navUpdateSupportsDirectUpgrade(180, 250, 180))
        assertFalse(navUpdateSupportsDirectUpgrade(81, 250, 82))
        assertFalse(navUpdateSupportsDirectUpgrade(180, 179, 143))
        assertFalse(navUpdateSupportsDirectUpgrade(180, 180, 143))
    }

    @Test fun interruptedTransferOnlyAppendsExact206OtherwiseRestartsOrFails() {
        assertEquals(NavUpdateTransferMode.APPEND,
            navUpdateTransferMode(206, 4096, 10_000, "bytes 4096-9999/10000"))
        assertEquals(NavUpdateTransferMode.RESTART,
            navUpdateTransferMode(200, 4096, 10_000, null))
        for (range in listOf("bytes 0-9999/10000", "bytes 4097-9999/10000",
            "bytes 4096-10000/10000", "bytes 4096-9999/9999", "bytes */10000")) {
            assertThrows(IOException::class.java) {
                navUpdateTransferMode(206, 4096, 10_000, range)
            }
        }
        assertThrows(IOException::class.java) {
            navUpdateTransferMode(416, 4096, 10_000, "bytes */10000")
        }
    }

    @Test fun releaseMustKeepSameApplicationIdentityAndManualInstallConsent() {
        val gradle = java.io.File("build.gradle.kts").readText()
        val manifest = java.io.File("src/main/AndroidManifest.xml").readText()
        val installer = java.io.File("src/main/java/com/navtasks/app/NavUpdateInstaller.kt").readText()
        assertTrue(gradle.contains("applicationIdSuffix = \".glass\""))
        assertTrue(gradle.contains("isDebuggable = false"))
        assertTrue(manifest.contains("android.permission.REQUEST_INSTALL_PACKAGES"))
        assertTrue(installer.contains("canRequestPackageInstalls()"))
        assertTrue(installer.contains("MessageDigest.isEqual(installedDigest, archiveDigest)"))
        assertTrue(installer.contains("FileProvider.getUriForFile"))
    }
}

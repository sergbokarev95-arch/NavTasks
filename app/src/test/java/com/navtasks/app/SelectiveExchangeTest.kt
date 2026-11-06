package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SelectiveExchangeTest {
    private val day = LocalDate.of(2026, 11, 6)
    private val vessel = NavProfile(id = "vessel-01", title = "Test vessel",
        kind = NavProfileKind.VESSEL)

    @Test fun selectiveImportKeepsExistingRecordsAndHistoryAndRemovesSenderCompletion() {
        val localTask = TaskTemplate(id = "local", title = "Compass Error",
            frequency = Frequency.DAILY, startDate = day.minusDays(20))
        val localCompletion = TaskRun(localTask.id, day.minusDays(1), emptyList(),
            completed = true, completedAt = "2026-11-05T09:00:00Z")
        val originalService = ServiceEntry(id = "old-service",
            equipment = "Steering gear", lastDate = day.minusMonths(6),
            nextDate = day.plusMonths(6))
        val snapshot = ProfileSnapshot(vessel, listOf(localTask),
            listOf(localCompletion), emptyList(), day.toString(),
            services = listOf(originalService))

        val incomingTask = TaskTemplate(id = "remote", title = "Medical Inventory",
            frequency = Frequency.MONTHLY, startDate = day.minusDays(10),
            dayOfMonth = 31,
            checklist = listOf(CheckItem(id = "remote-check", title = "Count stores",
                comment = "Check pharmacy")))
        val incomingWork = WorkOrderEntry(id = "remote-amos", reference = "AMOS-100",
            title = "Gyro adjustment", dueDate = day.plusDays(4),
            createdAt = day.minusMonths(6), completedAt = day.minusDays(2),
            closedAt = day.minusDays(1), checklist = listOf(
                CheckItem(id = "check1", title = "Compare headings")),
            checkedItemIds = setOf("check1"))
        val incomingService = ServiceEntry(id = "remote-service",
            equipment = "GYRO COMPASSES SERVICE", lastDate = day.minusMonths(2),
            nextDate = day.plusMonths(8), periodMonths = 12,
            requestNumber = "R-123", completedAt = day.minusDays(1))
        val incomingHandover = HandoverSection(id = "remote-section",
            title = "Navigation", entries = listOf(
                HandoverEntry(id = "remote-entry", title = "Gyro certificate",
                    notes = "In locker", mark = HandoverMark.COMPLETED,
                    completedDate = day.minusDays(2))))
        val incoming = SelectiveBundle(tasks = listOf(incomingTask),
            workOrders = listOf(incomingWork), services = listOf(incomingService),
            sections = listOf(incomingHandover))
        val merged = mergeSelective(snapshot, incoming)
        assertEquals(1, merged.added.tasks)
        assertEquals(1, merged.added.amos)
        assertEquals(1, merged.added.services)
        assertEquals(1, merged.added.sections)
        assertEquals(1, merged.added.handoverEntries)
        assertEquals(snapshot.runs, merged.snapshot.runs)
        assertEquals(snapshot.lastVisit, merged.snapshot.lastVisit)
        assertEquals(localTask, merged.snapshot.templates.first())
        assertEquals(originalService, merged.snapshot.services.first())

        val task = merged.snapshot.templates.last()
        assertNotEquals(incomingTask.id, task.id)
        assertNotEquals(incomingTask.checklist.single().id, task.checklist.single().id)
        assertEquals("Check pharmacy", task.checklist.single().comment)
        assertEquals(31, task.dayOfMonth)
        val work = merged.snapshot.workOrders.single()
        assertNotEquals(incomingWork.id, work.id)
        assertEquals("AMOS-100", work.reference)
        assertTrue(work.executionHistory.isEmpty())
        assertTrue(work.checkedItemIds.isEmpty())
        assertNull(work.completedAt)
        assertNull(work.closedAt)
        assertNotEquals("check1", work.checklist.single().id)
        val service = merged.snapshot.services.last()
        assertTrue(service.serviceHistory.isEmpty())
        assertEquals("", service.requestNumber)
        assertNull(service.completedAt)
        assertNull(service.closedAt)
        val entry = merged.snapshot.sections.single().entries.single()
        assertEquals(HandoverMark.OPEN, entry.mark)
        assertNull(entry.completedDate)
        assertEquals("In locker", entry.notes)
    }

    @Test fun sameNameAndAmosReferenceAreAppendOnlyEvenWithDifferentCase() {
        val snapshot = ProfileSnapshot(vessel,
            templates = listOf(TaskTemplate(id = "a", title = "SEA LOG",
                frequency = Frequency.DAILY)),
            runs = emptyList(), sections = listOf(HandoverSection(
                title = "Navigation", entries = listOf(HandoverEntry(
                    title = "ECDIS", notes = "Keep logs",
                    mark = HandoverMark.COMPLETED)))),
            lastVisit = day.toString(),
            services = listOf(ServiceEntry(equipment = "Gyro Service",
                lastDate = day.minusMonths(2), nextDate = day.plusMonths(10))),
            workOrders = listOf(WorkOrderEntry(reference = "X-001",
                title = "Gyro adjust", dueDate = day.plusDays(3))))
        val incoming = SelectiveBundle(
            tasks = listOf(TaskTemplate(title = "sea log",
                frequency = Frequency.DAILY)),
            services = listOf(ServiceEntry(equipment = "GYRO SERVICE",
                lastDate = day.minusDays(2), nextDate = day.plusMonths(1))),
            workOrders = listOf(WorkOrderEntry(reference = "x-001",
                title = "Other job", dueDate = day.plusDays(8))),
            sections = listOf(HandoverSection(title = "navigation",
                entries = listOf(
                    HandoverEntry(title = "ecdis", notes = "Keep logs"),
                    HandoverEntry(title = "Radar", notes = "Test daily")))),
        )
        val result = mergeSelective(snapshot, incoming)
        assertEquals(0, result.added.tasks)
        assertEquals(0, result.added.amos)
        assertEquals(0, result.added.services)
        assertEquals(0, result.added.sections)
        assertEquals(1, result.added.handoverEntries)
        assertEquals(2, result.snapshot.sections.single().entries.size)
        assertEquals(HandoverMark.COMPLETED,
            result.snapshot.sections.single().entries.first().mark)
        assertEquals(1, result.snapshot.workOrders.size)
        assertEquals(1, result.snapshot.services.size)
    }

    @Test fun repeatedImportIsIdempotentAndHistoryRemainsOriginal() {
        val source = SelectiveBundle(
            tasks = listOf(TaskTemplate(title = "Weekly GMDSS",
                frequency = Frequency.WEEKLY)),
            workOrders = listOf(WorkOrderEntry(title = "Gyro adjust",
                dueDate = day.plusDays(6))),
            services = listOf(ServiceEntry(equipment = "Gyro service",
                lastDate = day, nextDate = day.plusMonths(6))),
            sections = listOf(HandoverSection(title = "Navigation",
                entries = listOf(HandoverEntry(title = "Port folder")))),
        )
        val original = ProfileSnapshot(vessel, emptyList(), emptyList(),
            emptyList(), day.toString())
        val first = mergeSelective(original, source)
        val second = mergeSelective(first.snapshot, source)
        assertEquals(0, second.added.total)
        assertEquals(first.snapshot, second.snapshot)
    }

    @Test fun searchRespectsTitlesOnlyButHandoverSearchesAllSectionsAndNotes() {
        val orders = listOf(
            WorkOrderEntry(title = "GYRO COMPASSES SERVICE", dueDate = day),
            WorkOrderEntry(title = "Gyro adjust", dueDate = day),
            WorkOrderEntry(title = "Radar check", dueDate = day,
                comment = "gyro backup available"))
        assertEquals(2, searchAmosTitles(orders, " GyRo ").size)
        assertTrue(searchAmosTitles(orders, "backup").isEmpty())
        val maint = listOf(
            ServiceEntry(equipment = "Gyro annual", lastDate = day,
                nextDate = day.plusMonths(12), comment = "compass"),
            ServiceEntry(equipment = "Radar", lastDate = day,
                nextDate = day.plusMonths(12), comment = "gyro test"))
        assertEquals(listOf("Gyro annual"),
            searchServiceTitles(maint, "gyro").map { it.equipment })
        val sections = listOf(
            HandoverSection(title = "Navigation", entries = listOf(
                HandoverEntry(title = "Gyro", notes = "in cabinet"),
                HandoverEntry(title = "Chart", notes = "near bridge"))),
            HandoverSection(title = "Medicines", entries = listOf(
                HandoverEntry(title = "Medical log", notes = "gyro medication note"),
                HandoverEntry(title = "Expiry", notes = "next month"))),
        )
        val matches = searchHandoverSections(sections, "gyro")
        assertEquals(2, matches.size)
        assertEquals(1, matches[0].entries.size)
        assertEquals(1, matches[1].entries.size)
        assertEquals(2, searchHandoverSections(sections, "navigation").first().entries.size)
        assertTrue(searchHandoverSections(sections, "NOT FOUND").isEmpty())
        assertEquals(sections, searchHandoverSections(sections, ""))
    }

    @Test fun sameAmosTitleWithDifferentNumbersOrSchedulesCanBothBeImported() {
        val existing = ProfileSnapshot(vessel, emptyList(), emptyList(),
            emptyList(), day.toString(), workOrders = listOf(
                WorkOrderEntry(reference = "R-100", title = "Gyro service",
                    dueDate = day.plusDays(4)),
            ))
        val imported = SelectiveBundle(workOrders = listOf(
            WorkOrderEntry(reference = "R-101", title = "Gyro service",
                dueDate = day.plusDays(4)),
            WorkOrderEntry(reference = "", title = "Gyro service",
                dueDate = day.plusDays(12)),
            WorkOrderEntry(reference = "", title = "Gyro service",
                dueDate = day.plusDays(30)),
        ))
        val first = mergeSelective(existing, imported)
        assertEquals(3, first.added.amos)
        assertEquals(4, first.snapshot.workOrders.size)
        assertEquals(0, mergeSelective(first.snapshot, imported).added.amos)
    }

    @Test fun personalProfileCannotReceiveVesselData() {
        val personal = ProfileSnapshot(
            NavProfile("personal", "Личное", NavProfileKind.PERSONAL),
            emptyList(), emptyList(), emptyList(), day.toString())
        assertTrue(runCatching {
            mergeSelective(personal, SelectiveBundle(tasks = listOf(
                TaskTemplate(title = "Private", frequency = Frequency.DAILY))))
        }.isFailure)
    }
}

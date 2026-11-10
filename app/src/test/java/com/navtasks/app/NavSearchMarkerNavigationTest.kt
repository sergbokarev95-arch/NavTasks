package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavSearchMarkerNavigationTest {
    @Test fun marksEachOccurrenceWithCaseInsensitiveNonOverlappingRanges() {
        assertEquals(listOf(0..2, 9..11), navSearchRanges("AMOS and amos", "aMo"))
        assertEquals(listOf(0..3, 6..9), navSearchRanges("Тест: тест", "ТЕСТ"))
        assertTrue(navSearchRanges("hello", "  ").isEmpty())
        assertTrue(navSearchRanges("hello", "missing").isEmpty())
    }

    @Test fun arrowsWrapAndZeroMatchesAreInert() {
        assertEquals(0, navSearchNextIndex(2, 3, 1))
        assertEquals(2, navSearchNextIndex(0, 3, -1))
        assertEquals(1, navSearchNextIndex(0, 3, 1))
        assertEquals(0, navSearchNextIndex(5, 0, 1))
        assertEquals(0, navSearchNextIndex(5, 0, -1))
    }

    @Test fun handoverCountsEveryOccurrenceInsideOneLongNote() {
        val sections = listOf(
            HandoverSection(
                id = "navigation", title = "Navigation",
                entries = listOf(
                    HandoverEntry(id = "radar", title = "Radar",
                        notes = "работает работал работает работает"),
                    HandoverEntry(id = "other", title = "X-band",
                        notes = "работает"),
                ),
            ),
        )
        val hits = navHandoverSearchHits(sections, "РАБОТА")
        assertEquals(5, hits.size)
        assertTrue(hits.all { it.field == NavHandoverField.ENTRY_NOTES })
        assertEquals(listOf("radar", "radar", "radar", "radar", "other"),
            hits.map { it.entryId })
        assertEquals(0, navSearchNextIndex(4, 5, 1))
        assertEquals(4, navSearchNextIndex(0, 5, -1))
        assertEquals(2, hits.map { it.entryId }.distinct().size)
    }

    @Test fun handoverSearchCountsCategoryTitleEntryTitleAndNoteSeparately() {
        val sections = listOf(
            HandoverSection(id = "navigation", title = "Navigation",
                entries = listOf(
                    HandoverEntry(id = "e1", title = "Radar radar", notes = "Radar test"),
                )),
        )
        val hits = navHandoverSearchHits(sections, "radar")
        assertEquals(3, hits.size)
        assertEquals(listOf(
            NavHandoverField.ENTRY_TITLE, NavHandoverField.ENTRY_TITLE,
            NavHandoverField.ENTRY_NOTES,
        ), hits.map { it.field })
        assertEquals(listOf(0, 6, 0), hits.map { it.start })
    }

    @Test fun referenceCountsAllMatchesWithinSamePlaceholder() {
        val rows = listOf(
            NavReferenceSearchRow("sire", "SIRE", "SIRE inspections", "SIRE materials"),
            NavReferenceSearchRow("viq", "VIQ7", "Archive", "Pending"),
        )
        val hits = navReferenceSearchHits(rows, "sire")
        assertEquals(3, hits.size)
        assertTrue(hits.all { it.sectionId == "sire" })
        assertEquals(listOf(
            NavReferenceField.TITLE, NavReferenceField.SUBTITLE, NavReferenceField.FOOTER,
        ), hits.map { it.field })
    }

    @Test fun searchStartsOnlyAtTwoCharacters() {
        assertEquals("", navEffectiveSearchQuery("р"))
        assertEquals("", navEffectiveSearchQuery("  A "))
        assertEquals("РА", navEffectiveSearchQuery("  РА  "))
        assertTrue(navSearchRanges("работа работа", "р").isEmpty())
        assertTrue(navHandoverSearchHits(emptyList(), "a").isEmpty())
        assertTrue(navReferenceSearchHits(emptyList(), "s").isEmpty())
    }
}

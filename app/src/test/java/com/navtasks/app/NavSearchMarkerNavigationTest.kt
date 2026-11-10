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

    @Test fun HandoverSearchFindsHeadingsAndNotesSeparately() {
        val sections = listOf(
            HandoverSection(
                id = "navigation",
                title = "Navigation",
                entries = listOf(
                    HandoverEntry(id = "e1", title = "ECDIS", notes = "Charts ready"),
                    HandoverEntry(id = "e2", title = "Radar", notes = "Navx fix pending"),
                ),
            ),
            HandoverSection(id = "other", title = "Other"),
        )
        assertEquals(listOf(NavHandoverSearchHit("navigation", "e2")),
            navHandoverSearchHits(sections, "NAVX"))
        assertEquals(listOf(NavHandoverSearchHit("navigation")),
            navHandoverSearchHits(sections, "navigation"))
        assertTrue(navHandoverSearchHits(sections, "not found").isEmpty())
    }

    @Test fun multipleOccurrencesInOneEntryAreNotExtraNavigationCards() {
        val rows = listOf("Radar radar radar", "Radar")
        val matchedCards = rows.count { navSearchRanges(it, "radar").isNotEmpty() }
        val occurrences = rows.sumOf { navSearchRanges(it, "radar").size }
        assertEquals(2, matchedCards)
        assertEquals(4, occurrences)
    }
}

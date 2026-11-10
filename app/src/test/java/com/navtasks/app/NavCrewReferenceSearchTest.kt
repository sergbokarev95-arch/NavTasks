package com.navtasks.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class NavCrewReferenceSearchTest {
    @Test fun referenceSearchMatchesExistingPlaceholdersInEitherLanguage() {
        assertTrue(referencePlaceholderMatches("", "SIRE 2.0", "Подготовка к инспекциям"))
        assertTrue(referencePlaceholderMatches("  sire  ", "SIRE 2.0", "Inspection preparation"))
        assertTrue(referencePlaceholderMatches("ИНСПЕКЦ", "SIRE 2.0", "Подготовка к инспекциям"))
        assertTrue(referencePlaceholderMatches("viq7", "VIQ7", "Архивный справочник"))
        assertTrue(referencePlaceholderMatches("archive", "VIQ7", "Archive"))
        assertFalse(referencePlaceholderMatches("AMOS", "SIRE 2.0", "Inspection preparation"))
    }

    @Test fun largerFullPageNotesPreserveCompactEditorTypography() {
        assertEquals(17, NavRichNoteTypography.fullPageBodySp)
        assertEquals(25, NavRichNoteTypography.fullPageLineSp)
        assertEquals(14, NavRichNoteTypography.compactBodySp)
        assertEquals(20, NavRichNoteTypography.compactLineSp)
    }
}

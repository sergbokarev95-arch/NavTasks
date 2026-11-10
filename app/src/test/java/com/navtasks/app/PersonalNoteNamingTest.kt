package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalNoteNamingTest {
    @Test fun firstUntitledPersonalNoteGetsNumberOne() {
        assertEquals("Заметка 1", nextPersonalNoteTitle(emptyList(), UiLanguage.RU))
        assertEquals("Note 1", nextPersonalNoteTitle(emptyList(), UiLanguage.EN))
    }

    @Test fun numbersIncrementAcrossSectionsAndBothLanguages() {
        val sections = listOf(
            HandoverSection(title = "Разное", entries = listOf(
                HandoverEntry(title = "Заметка 1", notes = "text"),
                HandoverEntry(title = "Custom title", notes = "text"),
            )),
            HandoverSection(title = "More", entries = listOf(
                HandoverEntry(title = "Note 4", notes = "text"),
                HandoverEntry(title = "Заметка 3", notes = "text"),
            )),
        )
        assertEquals("Заметка 5", nextPersonalNoteTitle(sections, UiLanguage.RU))
        assertEquals("Note 5", nextPersonalNoteTitle(sections, UiLanguage.EN))
    }
}

package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NavPersonalVisualTokensTest {
    @Test fun personalStyleMatchesProfilesWithoutChangingVesselHandover() {
        assertEquals(9f, NavPersonalVisualTokens.headerTopDp, 0f)
        assertEquals(Color.White, NavPersonalVisualTokens.cardSurface(false, false))
        assertEquals(Color(0xFF202022), NavPersonalVisualTokens.cardSurface(true, false))
        assertEquals(Color(0xFFF2F2F5), NavPersonalVisualTokens.cardSurface(false, true))
        assertEquals(Color(0xFF2A2A2C), NavPersonalVisualTokens.cardSurface(true, true))
        val root = "src/main/java/com/navtasks/app/"
        val h = File(root + "HandoverUi.kt").readText()
        assertTrue(h.contains("if (personalMode) NavPersonalVisualTokens.headerTopDp.dp else 8.dp"))
        assertTrue(h.contains(".padding(start = 16.dp, top = headerTop)"))
        assertTrue(h.contains("if (personalMode) Modifier.personalNoteCard(p, 19.dp)"))
        assertTrue(h.contains("if (personalMode) Modifier.personalNoteCard(p, 13.dp, nested = true)"))
        assertTrue(h.contains("else Modifier.personalNoteCard(p, 15.dp, nested = true)"))
        assertTrue(h.contains("if (personalMode) openEntryEditor(section.id, entry)"))
        assertTrue(h.contains("handover.saveEntry(sectionId, savedTitle, plain, entryId, html, photos)"))
        val editor = File(root + "HandoverFullNoteEditor.kt").readText()
        assertTrue(editor.contains("if (personalMode) Box(Modifier.personalNoteCard(p, 13.dp))"))
        assertTrue(editor.contains("saveError = !onSave(title, plain, html, photos)"))
        val profiles = File(root + "ProfilesUi.kt").readText()
        assertTrue(profiles.contains(".padding(start = 16.dp, top = 9.dp)"))
    }
}

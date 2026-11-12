package com.navtasks.app

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavRichNotePaletteTest {
    @Test fun paletteIncludesNeutralColorsForEveryTheme() {
        for (dark in listOf(false, true)) {
            val swatches = navRichNoteSwatches(navPalette(dark))
            assertEquals(8, swatches.size)
            assertTrue(Color.Black in swatches)
            assertTrue(Color.White in swatches)
            assertTrue(navPalette(dark).blue in swatches)
            assertEquals(8, swatches.distinct().size)
        }
    }
}

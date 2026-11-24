package com.navtasks.app

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class NavPrimaryProfileHeaderTest {
    @Test fun primaryHeaderUsesFullProfileWidthAndNeutralInkWithoutBrand() {
        val ui = File("src/main/java/com/navtasks/app/NavTasksUi.kt").readText()
        val header = ui.substringAfter(".primaryTabSwipe(tab, ::openPrimaryTab)) {")
            .substringBefore("label = \"primaryTabSlide\"")
        assertFalse(header.contains("NavBrandLabel("))
        assertTrue(header.contains("profiles.active.title, color = palette.text"))
        assertTrue(header.contains("fontSize = 16.sp, maxLines = 1"))
        assertTrue(header.contains("Modifier.weight(1f, fill = false)"))
        assertFalse(header.contains("widthIn(max = 116.dp)"))
        assertTrue(header.contains("NavGlassGlyph(\"chevron-down\", palette.text, Modifier.size(14.dp))"))
        assertTrue(header.contains("glassClickable { showProfiles = true }"))
        assertTrue(header.contains("fontSize = 26.sp, color = palette.text"))
        assertTrue(header.contains("showSettings = true"))
        // Calendar intentionally no longer displays the old NavTasks brand.
        val calendarHeader = ui.substringAfter("} else if (showCalendar) {")
            .substringBefore("PlanScreenV2(")
        assertFalse(calendarHeader.contains("NavBrandLabel("))
        assertTrue(calendarHeader.contains("localized(\"План\", \"Plan\", lang)"))
    }
}

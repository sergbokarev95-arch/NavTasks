package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Test

class NavWidgetDisplayTest {
    @Test fun compactShowsTwoPendingTasksBeforeOverflow() {
        assertEquals(0, widgetOverflowCount(0, wide = false))
        assertEquals(0, widgetOverflowCount(1, wide = false))
        assertEquals(0, widgetOverflowCount(2, wide = false))
        assertEquals(2, widgetOverflowCount(4, wide = false))
    }

    @Test fun wideShowsThreePendingTasksBeforeOverflow() {
        assertEquals(0, widgetOverflowCount(0, wide = true))
        assertEquals(0, widgetOverflowCount(3, wide = true))
        assertEquals(1, widgetOverflowCount(4, wide = true))
        assertEquals(7, widgetOverflowCount(10, wide = true))
    }
}

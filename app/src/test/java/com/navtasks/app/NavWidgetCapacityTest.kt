package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Test

class NavWidgetCapacityTest {
    @Test fun shortWidgetsNeverForceFourRowsIntoRestrictedHeight() {
        assertEquals(2, widgetTaskCapacity(110))
        assertEquals(2, widgetTaskCapacity(139))
    }

    @Test fun tallHomeWidgetsUseSpareSpaceForActualTasks() {
        assertEquals(3, widgetTaskCapacity(140))
        assertEquals(3, widgetTaskCapacity(169))
        assertEquals(4, widgetTaskCapacity(170))
        assertEquals(4, widgetTaskCapacity(250))
    }

    @Test fun footerCountsTasksBeyondVisibleRowsWithoutGoingNegative() {
        assertEquals(7, widgetHiddenTaskCount(10, 3))
        assertEquals(6, widgetHiddenTaskCount(10, 4))
        assertEquals(0, widgetHiddenTaskCount(2, 4))
        assertEquals(0, widgetHiddenTaskCount(0, 2))
    }
}

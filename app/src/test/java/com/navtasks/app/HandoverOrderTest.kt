package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Test

class HandoverOrderTest {
    @Test fun completedAndStruckStayVisibleAtEnd() {
        val items = listOf(
            HandoverEntry(title = "complete", mark = HandoverMark.COMPLETED),
            HandoverEntry(title = "pending"),
            HandoverEntry(title = "struck", mark = HandoverMark.STRUCK),
            HandoverEntry(title = "next"),
        )
        assertEquals(listOf("pending", "next", "complete", "struck"),
            orderedHandoverEntries(items).map { it.title })
    }
}

package com.navtasks.app

import java.io.IOException
import org.junit.Assert.*
import org.junit.Test

/** No server is needed to verify the security-critical resume decision. */
class NavUpdateResumePolicyTest {
    @Test fun newDownloadAcceptsOnlyWhole200() {
        assertEquals(NavUpdateTransferMode.RESTART,
            navUpdateTransferMode(200, 0, 28_501_761L, null))
        assertEquals(NavUpdateTransferMode.RESTART,
            navUpdateTransferMode(200, 5_000_000L, 28_501_761L, null))
    }

    @Test fun exact206CanAppendRequestedRange() {
        assertEquals(NavUpdateTransferMode.APPEND,
            navUpdateTransferMode(206, 5_000_000L, 28_501_761L,
                "bytes 5000000-28501760/28501761"))
        assertEquals(NavUpdateTransferMode.APPEND,
            navUpdateTransferMode(206, 64_000L, 128_000L, "bytes 64000-127999/128000"))
    }

    @Test fun mismatchedOrUnrequestedPartialAlwaysFailsClosed() {
        val cases = listOf(
            Triple(206, 0L, "bytes 0-99/100"),
            Triple(206, 100L, "bytes 99-999/1000"),
            Triple(206, 100L, "bytes 100-999/999"),
            Triple(206, 100L, "bytes 100-1000/1000"),
            Triple(206, 100L, "bytes 100-99/1000"),
            Triple(206, 100L, "items 100-999/1000"),
            Triple(206, 100L, ""),
            Triple(416, 100L, "bytes */1000"),
            Triple(500, 100L, null),
        )
        for ((status, offset, header) in cases) {
            assertThrows("Unexpected acceptance: $status/$offset/$header",
                IOException::class.java) {
                navUpdateTransferMode(status, offset, 1000L, header)
            }
        }
    }

    @Test fun resumeRequiresSameSignedCandidateMetadataAndRetainsFinalChecks() {
        val x = NavUpdateChecker.Candidate("0.4.152-test1", 176, 174,
            "ru", "en", "https://github.com/sergbokarev95-arch/NavTasks-Updates/releases/download/test-v0.4.152/NavTasks_0.4.152-test1.apk",
            "a".repeat(64), 28_501_761L, "b".repeat(64))
        val y = x.copy(sha256 = "c".repeat(64))
        val z = x.copy(apkUrl = "https://github.com/sergbokarev95-arch/NavTasks-Updates/releases/download/test-v0.4.152/other.apk")
        assertNotEquals(navUpdateResumeIdentity(x), navUpdateResumeIdentity(y))
        assertNotEquals(navUpdateResumeIdentity(x), navUpdateResumeIdentity(z))
        val installer = java.io.File("src/main/java/com/navtasks/app/NavUpdateInstaller.kt").readText()
        assertTrue(installer.contains("FileOutputStream(partial, append)"))
        assertTrue(installer.contains("connection.getHeaderField(\"Content-Range\")"))
        assertTrue(installer.contains("identity.length() > 2048L"))
        assertTrue(installer.contains("verifyArchive(context, apk, update)"))
        assertTrue(installer.contains("MessageDigest.isEqual(installedDigest, archiveDigest)"))
        assertTrue(installer.contains("navUpdateAllowedAssetHost(current)"))
        assertTrue(installer.contains("if (partial.length() > update.sizeBytes)"))
    }
}

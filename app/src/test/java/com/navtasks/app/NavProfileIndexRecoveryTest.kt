package com.navtasks.app

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/** Pure tests: no Android context and no real user's SharedPreferences. */
class NavProfileIndexRecoveryTest {
    private fun profile(id: String, kind: NavProfileKind = NavProfileKind.VESSEL) =
        NavProfile(id, id.uppercase(), kind)

    private fun index(vararg items: NavProfile): String =
        JSONArray().apply { items.forEach { put(it.toJson()) } }.toString()

    @Test fun twoDifferentShipsWithSharedDefaultIdMustNeverReplaceEachOther() {
        val local = listOf(
            NavProfile("ship-default", "Yuri Senkevich", NavProfileKind.VESSEL),
            NavProfile("personal", "Личное", NavProfileKind.PERSONAL),
        )
        val foreign = NavProfile("ship-default", "2/O Kapitan Kostichev",
            NavProfileKind.VESSEL)
        assertNull(matchingProfileByName(local, foreign))
        assertEquals("Yuri Senkevich", local.first().title)
    }

    @Test fun onlySameTrimmedCaseInsensitiveNameAndKindMayReplace() {
        val local = listOf(
            NavProfile("ship-default", "2/O Kapitan Kostichev", NavProfileKind.VESSEL),
            NavProfile("personal", "Личное", NavProfileKind.PERSONAL),
        )
        val foreign = NavProfile("other-id", " 2/o kapitan kostichev ",
            NavProfileKind.VESSEL)
        assertEquals(local.first(), matchingProfileByName(local, foreign))
        assertNull(matchingProfileByName(local, NavProfile("personal",
            "Other ship", NavProfileKind.VESSEL)))
        assertNull(matchingProfileByName(local, NavProfile("ship-default",
            "2/O Kapitan Kostichev", NavProfileKind.PERSONAL)))
    }

    @Test fun verifiedPrimaryAlwaysWinsOverOlderBackup() {
        val current = index(profile("ship-1"), profile("ship-2"),
            profile("personal", NavProfileKind.PERSONAL))
        val old = index(profile("ship-1"), profile("personal", NavProfileKind.PERSONAL))
        val r = navChooseProfileIndex(current, old)
        assertFalse(r.fromBackup)
        assertEquals(current, r.raw)
        assertEquals(listOf("ship-1", "ship-2", "personal"), r.items.map { it.id })
    }

    @Test fun missingOrCorruptedMainIndexRecoversOnlyVerifiedBackup() {
        val good = index(profile("ship-1"), profile("ship-2"),
            profile("personal", NavProfileKind.PERSONAL))
        for (corrupt in listOf(null, "", "{broken", "[]", "[{}]",
            index(profile("duplicate"), profile("duplicate")))) {
            val r = navChooseProfileIndex(corrupt, good)
            assertTrue(r.fromBackup)
            assertEquals(good, r.raw)
            assertEquals(listOf("ship-1", "ship-2", "personal"), r.items.map { it.id })
        }
    }

    @Test fun noVerifiedCopyFailsClosedInsteadOfResettingProfiles() {
        for (bad in listOf(null, "", "[]", "{broken", "[1]",
            index(profile("duplicate"), profile("duplicate")))) {
            assertThrows(IllegalStateException::class.java) {
                navChooseProfileIndex("{missing", bad)
            }
        }
    }

    @Test fun duplicateAndInvalidProfileRecordsNeverQualifyAsSafetyCopy() {
        assertThrows(IllegalArgumentException::class.java) {
            navValidateProfileIndex("[]")
        }
        assertThrows(IllegalArgumentException::class.java) {
            navValidateProfileIndex(index(profile("ship-1"), profile("ship-1")))
        }
        assertThrows(IllegalArgumentException::class.java) {
            navValidateProfileIndex(index(profile("has space")))
        }
        assertThrows(Exception::class.java) {
            navValidateProfileIndex(JSONArray().put(
                JSONObject().put("id", "id").put("title", "Vessel")
                    .put("kind", "UNKNOWN")).toString())
        }
        assertThrows(IllegalArgumentException::class.java) {
            navValidateProfileIndex(" ".repeat(512_001))
        }
    }

    @Test fun safetyCopyIsWrittenBeforePrimaryAndNeverErasesUserData() {
        val source = java.io.File("src/main/java/com/navtasks/app/ProfileStore.kt").readText()
        val save = source.substringAfter("private fun saveIndex(")
            .substringBefore("\n    fun select(")
        assertTrue(save.indexOf("safeIndex.edit()") < save.indexOf("index.edit()"))
        assertTrue(save.contains("Could not back up profile index"))
        assertTrue(source.contains("navChooseProfileIndex("))
        assertFalse(source.contains(".getOrDefault(emptyList())"))
        assertTrue(source.contains("if (verified.fromBackup"))
        assertFalse(save.contains(".clear()"))
        // Only the explicit user-confirmed delete path may clear a profile file.
        assertEquals(1, Regex("\\.edit\\(\\)\\.clear\\(\\)\\.commit\\(\\)")
            .findAll(source).count())
    }
}

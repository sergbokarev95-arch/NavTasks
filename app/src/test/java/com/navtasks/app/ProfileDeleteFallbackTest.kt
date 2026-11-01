package com.navtasks.app

import org.junit.Assert.assertEquals
import org.junit.Test

/** Deleting the original ship profile must never orphan the app. */
class ProfileDeleteFallbackTest {
    private val primary = NavProfile(
        ProfileStore.DEFAULT_SHIP, "Captain Kostichev", NavProfileKind.VESSEL)
    private val personal = NavProfile(
        ProfileStore.PERSONAL_ID, "Личное", NavProfileKind.PERSONAL)
    private val other = NavProfile(
        "ship-alternate", "Second vessel", NavProfileKind.VESSEL)

    @Test fun deletingPrimarySelectsAnotherVessel() {
        assertEquals(other.id, profileAfterDeletion(
            listOf(primary, personal, other), primary.id, primary.id))
    }

    @Test fun deletingLastShipLeavesPersonalAvailable() {
        assertEquals(personal.id, profileAfterDeletion(
            listOf(primary, personal), primary.id, primary.id))
    }

    @Test fun deletingNonactiveShipKeepsCurrentSelectedProfile() {
        assertEquals(primary.id, profileAfterDeletion(
            listOf(primary, personal, other), other.id, primary.id))
    }

    @Test fun deletingOriginalShipWhilePersonalIsActiveDoesNotSwitchToAnotherShip() {
        assertEquals(personal.id, profileAfterDeletion(
            listOf(primary, personal, other), primary.id, personal.id))
    }
}

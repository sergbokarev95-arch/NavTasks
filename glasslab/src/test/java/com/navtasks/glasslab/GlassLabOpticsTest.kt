package com.navtasks.glasslab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassLabOpticsTest {
    @Test
    fun cardOutlinesSurvivePreLensBlur() {
        assertTrue(GlassLabOptics.bar.blurDp > 0f)
        assertTrue(GlassLabOptics.bar.blurDp <= 3f)
        assertTrue(GlassLabOptics.bar.refractionAmountDp > GlassLabOptics.bar.refractionHeightDp)
    }

    @Test
    fun refractionIsBoundedToAvoidOldShimmerRegression() {
        assertTrue(GlassLabOptics.bar.refractionHeightDp <= 14f)
        assertTrue(GlassLabOptics.bar.refractionAmountDp <= 40f)
        assertTrue(GlassLabOptics.selection.refractionAmountDp < GlassLabOptics.bar.refractionAmountDp)
        assertTrue(GlassLabOptics.selection.blurDp <= GlassLabOptics.bar.blurDp)
        assertEquals(2.0f, GlassLabOptics.bar.blurDp, 0f)
    }
}

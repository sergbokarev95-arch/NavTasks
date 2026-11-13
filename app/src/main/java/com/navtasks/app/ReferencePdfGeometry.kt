package com.navtasks.app

import kotlin.math.floor
import kotlin.math.min

/** Page rendering dimensions in pixels, preserving its aspect ratio and memory limits. */
internal data class ReferencePdfPixelSize(val width: Int, val height: Int)

internal fun referencePdfPixelSize(
    pageWidth: Int,
    pageHeight: Int,
    requestedWidth: Int,
    maxWidth: Int = 2400,
    maxHeight: Int = 4096,
    maxPixels: Long = 7_000_000L,
): ReferencePdfPixelSize {
    require(pageWidth > 0 && pageHeight > 0) { "Invalid PDF page dimensions" }
    require(requestedWidth > 0 && maxWidth > 0 && maxHeight > 0 && maxPixels > 0)
    val aspect = pageHeight.toDouble() / pageWidth
    val byHeight = floor(maxHeight / aspect).toInt().coerceAtLeast(1)
    val byPixels = floor(kotlin.math.sqrt(maxPixels / aspect)).toInt().coerceAtLeast(1)
    val width = min(min(requestedWidth, maxWidth), min(byHeight, byPixels)).coerceAtLeast(1)
    val height = floor(width * aspect).toInt().coerceIn(1, maxHeight)
    require(width.toLong() * height <= maxPixels) { "PDF page exceeds render budget" }
    return ReferencePdfPixelSize(width, height)
}

/** The PDF search API returns points; page coordinates map into this bitmap. */
internal fun referencePdfRectPixels(
    rectLeft: Float,
    rectTop: Float,
    rectRight: Float,
    rectBottom: Float,
    pageWidthPoints: Int,
    pageHeightPoints: Int,
    outputWidthPixels: Float,
    outputHeightPixels: Float,
): FloatArray {
    require(pageWidthPoints > 0 && pageHeightPoints > 0)
    val sx = outputWidthPixels / pageWidthPoints
    val sy = outputHeightPixels / pageHeightPoints
    return floatArrayOf(
        rectLeft * sx, rectTop * sy, rectRight * sx, rectBottom * sy
    )
}

/**
 * Keep the touched point under the finger while the PDF page width changes.
 * Old behavior left scroll at zero, visually snapping to the upper-left corner.
 */
internal fun referencePdfFocalScroll(
    oldScroll: Int, focalPx: Float, previousZoom: Float,
    nextZoom: Float, maxScroll: Int,
): Int {
    require(previousZoom > 0f && nextZoom > 0f && maxScroll >= 0)
    return kotlin.math.round(((oldScroll + focalPx) *
        (nextZoom / previousZoom) - focalPx).toDouble())
        .toInt().coerceIn(0, maxScroll)
}

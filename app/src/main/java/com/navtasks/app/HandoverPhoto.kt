package com.navtasks.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.math.max

/**
 * Self-contained JPEG image stored with its Handover entry. Keeping only compact,
 * re-encoded image bytes (no external content Uri) preserves offline access, full
 * profile exports and selective Handover transfer without an extra file dependency.
 */
data class HandoverPhoto(
    val id: String = UUID.randomUUID().toString(),
    val jpegBase64: String,
    val widthPx: Int,
    val heightPx: Int,
) {
    fun jpegBytes(): ByteArray {
        require(jpegBase64.length <= MAX_BASE64_LENGTH) { "Handover photo too large" }
        val bytes = Base64.getDecoder().decode(jpegBase64)
        require(bytes.size in 16..MAX_JPEG_BYTES && bytes[0] == 0xff.toByte() &&
            bytes[1] == 0xd8.toByte() && bytes[2] == 0xff.toByte()) {
            "Invalid Handover JPEG"
        }
        return bytes
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("mime", "image/jpeg")
        .put("width", widthPx)
        .put("height", heightPx)
        .put("jpeg", jpegBase64)

    companion object {
        const val MAX_PER_ENTRY = 4
        const val MAX_JPEG_BYTES = 180_000
        const val MAX_PROFILE_IMAGE_BYTES = 4_000_000
        const val MAX_BASE64_LENGTH = ((MAX_JPEG_BYTES + 2) / 3) * 4

        fun fromJson(obj: JSONObject): HandoverPhoto {
            require(obj.optString("mime") == "image/jpeg") { "Unsupported Handover image type" }
            val width = obj.optInt("width")
            val height = obj.optInt("height")
            require(width in 1..1600 && height in 1..1600) { "Invalid Handover photo dimensions" }
            val photo = HandoverPhoto(
                id = obj.optString("id").takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
                jpegBase64 = obj.getString("jpeg"),
                widthPx = width,
                heightPx = height,
            )
            photo.jpegBytes() // Refuse corrupt imported photos instead of silently losing them.
            return photo
        }
    }
}

/** Android gallery decoding/compression. No EXIF or original URI is kept. */
internal object HandoverPhotoCodec {
    suspend fun fromUri(context: Context, uri: Uri): HandoverPhoto = withContext(Dispatchers.IO) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        var bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val w = info.size.width
            val h = info.size.height
            require(w in 1..20_000 && h in 1..20_000) { "Invalid photo dimensions" }
            val ratio = (1600f / max(w, h)).coerceAtMost(1f)
            decoder.setTargetSize(max(1, (w * ratio).toInt()), max(1, (h * ratio).toInt()))
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        var quality = 85
        repeat(9) {
            val buffer = ByteArrayOutputStream()
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, quality, buffer)) {
                "Could not compress photo"
            }
            val data = buffer.toByteArray()
            if (data.size <= HandoverPhoto.MAX_JPEG_BYTES) {
                return@withContext HandoverPhoto(
                    jpegBase64 = Base64.getEncoder().encodeToString(data),
                    widthPx = bitmap.width,
                    heightPx = bitmap.height,
                )
            }
            val shrunk = Bitmap.createScaledBitmap(bitmap,
                max(1, (bitmap.width * 0.82f).toInt()),
                max(1, (bitmap.height * 0.82f).toInt()), true)
            if (shrunk !== bitmap) bitmap.recycle()
            bitmap = shrunk
            quality = (quality - 6).coerceAtLeast(55)
        }
        bitmap.recycle()
        error("Photo cannot be safely reduced to 180 KB")
    }

    fun decodeForPreview(photo: HandoverPhoto): Bitmap? = runCatching {
        val bytes = photo.jpegBytes()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }.getOrNull()
}

package com.navtasks.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Small, horizontal thumbnails directly below the original Handover note. */
@Composable
internal fun HandoverPhotoStrip(
    photos: List<HandoverPhoto>, p: NavPalette, lang: UiLanguage,
    onRemove: ((String) -> Unit)? = null,
) {
    if (photos.isEmpty()) return
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        photos.forEach { photo ->
            Column {
                val bitmap = remember(photo.id, photo.jpegBase64) {
                    HandoverPhotoCodec.decodeForPreview(photo)?.asImageBitmap()
                }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = localized("Фото Handover", "Handover photo", lang),
                        modifier = Modifier.size(if (onRemove == null) 92.dp else 116.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(localized("Ошибка фото", "Invalid photo", lang),
                        color = p.warningText, fontSize = 12.sp)
                }
                if (onRemove != null) {
                    Spacer(Modifier.height(2.dp))
                    TextButton(onClick = { onRemove(photo.id) }) {
                        Text(localized("Удалить", "Remove", lang),
                            fontSize = 12.sp, color = p.systemRed)
                    }
                }
            }
        }
    }
}

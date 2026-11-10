package com.navtasks.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Informational only: same warm Material language as Today Completed. */
@Composable
internal fun NavInfoSectionBanner(
    p: NavPalette,
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(17.dp)
    Column(
        modifier.fillMaxWidth()
            .navGlass(p, radius = 17.dp, role = GlassRole.PANEL)
            .border(1.dp, p.warningText.copy(alpha = .52f), shape)
            .background(p.warningText.copy(alpha = if (p.dark) .10f else .055f), shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(title, color = p.warningText, fontSize = 15.sp,
            fontWeight = FontWeight.Medium)
        if (!description.isNullOrBlank()) {
            Text(description, color = p.muted, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

package com.horos.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.wear.compose.material3.MaterialTheme
import com.horos.wear.R

val WatchBackground = Color(0xFF0A0A12)
val WatchPrimaryText = Color(0xFFFFFFFF)
val WatchLabel = Color(0xFFA6A6B2)
val WatchSpentText = Color(0xFFD5D5DC)
val WatchMuted = Color(0xFF6B6B76)
val WatchTrack = Color(0xFF1C1C28)
val WatchAccent = Color(0xFF0077B6)
val WatchThumb = Color(0xFFFFFFFF)

val WatchOk = Color(0xFF30D158)
val WatchWarning = Color(0xFFF0A202)
val WatchOver = Color(0xFFE5484D)

val InterWear = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)

@Composable
fun HorosWearTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

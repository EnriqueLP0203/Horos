package com.horos.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.horos.app.R

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private val base = TextStyle(fontFamily = Inter, color = TextPrimary)

val Typography = Typography(
    displaySmall = base.copy(fontSize = 36.sp, fontWeight = FontWeight.Bold),
    headlineSmall = base.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
    titleLarge = base.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = base.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = base.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    bodyMedium = base.copy(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = base.copy(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = base.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = base.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = base.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
)

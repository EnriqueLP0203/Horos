package com.horos.wear.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.horos.wear.presentation.theme.InterWear
import com.horos.wear.presentation.theme.WatchBackground
import com.horos.wear.presentation.theme.WatchLabel
import com.horos.wear.presentation.theme.WatchOk
import com.horos.wear.presentation.theme.WatchOver
import com.horos.wear.presentation.theme.WatchPrimaryText
import com.horos.wear.presentation.theme.WatchSpentText
import com.horos.wear.presentation.theme.WatchTrack
import com.horos.wear.presentation.theme.WatchWarning
import java.util.Locale
import kotlin.math.abs

// Available screen: ring with today's remaining budget. Tapping the ring opens Adjust limit.
@Composable
fun GlanceScreen(
    limit: Double,
    spent: Double,
    remaining: Double,
    hasData: Boolean,
    onAdjustClick: () -> Unit
) {
    val status = budgetStatus(spent, limit)
    // The ring shows the fraction of the limit that is still left.
    val progress = if (hasData && limit > 0) (remaining / limit).coerceIn(0.0, 1.0).toFloat() else 0f

    Box(
        modifier = Modifier.fillMaxSize().background(WatchBackground),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = hasData,
                    onClick = onAdjustClick
                ),
            contentAlignment = Alignment.Center
        ) {
            BudgetRing(progress = progress, color = status.color)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Label("DISPONIBLE")
                if (hasData) {
                    Text(
                        formatMoney(remaining),
                        color = WatchPrimaryText,
                        fontFamily = InterWear,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    Label("MXN")
                } else {
                    Text(
                        "Abre Horos+ en tu teléfono",
                        color = WatchSpentText,
                        fontFamily = InterWear,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }

        if (hasData) {
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Gastado hoy ${formatMoney(spent)}",
                    color = WatchSpentText,
                    fontFamily = InterWear,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    status.label,
                    color = status.color,
                    fontFamily = InterWear,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .background(status.color.copy(alpha = 0.16f), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun BudgetRing(progress: Float, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = 8.dp.toPx()
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(stroke / 2, stroke / 2)
        drawArc(WatchTrack, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text,
        color = WatchLabel,
        fontFamily = InterWear,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp
    )
}

data class BudgetStatus(val label: String, val color: Color)

// Status by percentage used: <80% on track, 80–100% near the limit, >100% exceeded.
fun budgetStatus(spent: Double, limit: Double): BudgetStatus {
    val used = if (limit > 0) spent / limit else 0.0
    return when {
        used < 0.8 -> BudgetStatus("EN META", WatchOk)
        used <= 1.0 -> BudgetStatus("CERCA DEL LÍMITE", WatchWarning)
        else -> BudgetStatus("EXCEDIDO", WatchOver)
    }
}

fun formatMoney(value: Double): String {
    val text = String.format(Locale.US, "$%,.2f", abs(value))
    return if (value < 0) "-$text" else text
}

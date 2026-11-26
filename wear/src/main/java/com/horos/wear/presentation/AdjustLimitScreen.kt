package com.horos.wear.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.horos.wear.presentation.theme.InterWear
import com.horos.wear.presentation.theme.WatchAccent
import com.horos.wear.presentation.theme.WatchBackground
import com.horos.wear.presentation.theme.WatchLabel
import com.horos.wear.presentation.theme.WatchMuted
import com.horos.wear.presentation.theme.WatchPrimaryText
import com.horos.wear.presentation.theme.WatchThumb
import com.horos.wear.presentation.theme.WatchTrack
import kotlin.math.abs
import kotlin.math.roundToInt

private const val MIN_LIMIT = 100
private const val MAX_LIMIT = 1000
private const val STEP = 50
private const val CROWN_PIXELS_PER_STEP = 40f

// Rounds to the nearest $50 multiple within the allowed range.
private fun snap(value: Double): Int =
    ((value / STEP).roundToInt() * STEP).coerceIn(MIN_LIMIT, MAX_LIMIT)

// Adjust limit screen: starts at the current limit and sends the value when Confirmar is tapped.
@Composable
fun AdjustLimitScreen(
    currentLimit: Double,
    onConfirm: (Double) -> Unit,
    onBack: () -> Unit
) {
    var value by rememberSaveable { mutableIntStateOf(snap(currentLimit)) }
    var crownAccumulated by remember { mutableFloatStateOf(0f) }
    val focusRequester = remember { FocusRequester() }

    // On Wear OS a right swipe arrives as "back"; without this the app would close.
    BackHandler(onBack = onBack)
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WatchBackground)
            // Crown (optional): scroll pixels accumulate and every 40 px moves one step.
            .onRotaryScrollEvent { event ->
                crownAccumulated += event.verticalScrollPixels
                if (abs(crownAccumulated) >= CROWN_PIXELS_PER_STEP) {
                    val direction = if (crownAccumulated > 0) 1 else -1
                    value = (value + direction * STEP).coerceIn(MIN_LIMIT, MAX_LIMIT)
                    crownAccumulated = 0f
                }
                true
            }
            .focusRequester(focusRequester)
            .focusable()
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "AJUSTAR LÍMITE DIARIO",
            color = WatchLabel,
            fontFamily = InterWear,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
        Text(
            formatMoney(value.toDouble()),
            color = WatchPrimaryText,
            fontFamily = InterWear,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )
        Spacer(Modifier.height(8.dp))
        LimitSlider(value = value, onValueChange = { value = it })
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SmallLabel("MIN $$MIN_LIMIT")
            SmallLabel("PASO $$STEP")
            SmallLabel("MAX $1,000")
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .background(WatchAccent)
                .clickable { onConfirm(value.toDouble()) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Confirmar",
                color = WatchPrimaryText,
                fontFamily = InterWear,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

// Custom slider (track, fill and thumb) that responds to taps and horizontal drags.
@Composable
private fun LimitSlider(value: Int, onValueChange: (Int) -> Unit) {
    var widthPx by remember { mutableFloatStateOf(1f) }
    val density = LocalDensity.current
    val fraction = (value - MIN_LIMIT).toFloat() / (MAX_LIMIT - MIN_LIMIT)

    fun valueAt(x: Float): Int {
        val ratio = (x / widthPx).coerceIn(0f, 1f)
        return snap(MIN_LIMIT + ratio * (MAX_LIMIT - MIN_LIMIT).toDouble())
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(Unit) { detectTapGestures { onValueChange(valueAt(it.x)) } }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    onValueChange(valueAt(change.position.x))
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(Modifier.fillMaxWidth().height(8.dp).background(WatchTrack))
        Box(Modifier.fillMaxWidth(fraction).height(8.dp).background(WatchAccent))
        val thumbOffset = with(density) { (widthPx * fraction).toDp() } - 2.dp
        Box(
            Modifier
                .offset(x = thumbOffset.coerceAtLeast(0.dp))
                .size(width = 4.dp, height = 16.dp)
                .background(WatchThumb)
        )
    }
}

@Composable
private fun SmallLabel(text: String) {
    Text(text, color = WatchMuted, fontFamily = InterWear, fontSize = 10.sp)
}

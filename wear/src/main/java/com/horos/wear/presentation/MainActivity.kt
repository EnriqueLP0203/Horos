package com.horos.wear.presentation

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material3.AppScaffold
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.horos.wear.WearableConstants
import com.horos.wear.presentation.theme.HorosWearTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Main watch screen. It stores nothing of its own: it shows the latest state the
 * phone published to the Data Layer (feature 1) and sends the limit adjustment (feature 2).
 * Implements DataClient.OnDataChangedListener to know when the phone publishes.
 */
class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    private enum class Screen { GLANCE, ADJUST }

    // Compose-observable state; hasData tells "nothing received yet" apart from a $0 remaining.
    private var limit by mutableDoubleStateOf(0.0)
    private var spent by mutableDoubleStateOf(0.0)
    private var remaining by mutableDoubleStateOf(0.0)
    private var hasData by mutableStateOf(false)
    private var screen by mutableStateOf(Screen.GLANCE)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            HorosWearTheme {
                AppScaffold {
                    when (screen) {
                        Screen.GLANCE -> GlanceScreen(
                            limit = limit,
                            spent = spent,
                            remaining = remaining,
                            hasData = hasData,
                            onAdjustClick = { screen = Screen.ADJUST }
                        )
                        Screen.ADJUST -> AdjustLimitScreen(
                            currentLimit = limit,
                            onConfirm = ::sendNewLimit,
                            onBack = { screen = Screen.GLANCE }
                        )
                    }
                }
            }
        }
        loadLastBudget()
    }

    override fun onResume() {
        super.onResume()
        // Registers the listener on the DataClient to receive every update from the phone.
        Wearable.getDataClient(this).addListener(this)
        // Anything published while the app was paused does not reach the listener; it is read here.
        loadLastBudget()
    }

    override fun onPause() {
        super.onPause()
        // Removes the listener so no events arrive while the Activity is in the background.
        Wearable.getDataClient(this).removeListener(this)
    }

    /**
     * Feature 1 (DataClient, phone → watch).
     * Called every time a DataItem changes in the Data Layer. Only TYPE_CHANGED events
     * on PATH_BUDGET matter; deletions and other paths are ignored.
     * The system releases the event buffer when this method returns.
     */
    override fun onDataChanged(events: DataEventBuffer) {
        for (event in events) {
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == WearableConstants.PATH_BUDGET
            ) {
                applyBudget(event.dataItem)
            }
        }
    }

    /**
     * Reads the last stored DataItem when the app opens. The listener only reports new
     * changes, so without this the watch would stay empty until the next expense.
     */
    private fun loadLastBudget() {
        // dataItems returns a Task; await() waits for it inside the coroutine without freezing the UI.
        lifecycleScope.launch {
            try {
                val items = Wearable.getDataClient(this@MainActivity).dataItems.await()
                try {
                    items.firstOrNull { it.uri.path == WearableConstants.PATH_BUDGET }
                        ?.let(::applyBudget)
                } finally {
                    // A DataItemBuffer requested manually must be released to avoid leaking memory.
                    items.release()
                }
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo leer el último presupuesto", e)
            }
        }
    }

    // Turns the DataItem into a DataMap and copies its keys into the screen state.
    private fun applyBudget(item: DataItem) {
        val data = DataMapItem.fromDataItem(item).dataMap
        limit = data.getDouble(WearableConstants.KEY_DAILY_LIMIT)
        spent = data.getDouble(WearableConstants.KEY_SPENT_TODAY)
        remaining = data.getDouble(WearableConstants.KEY_REMAINING)
        hasData = true
    }

    /**
     * Feature 2 (MessageClient, watch → phone).
     * Sends the new limit as a one-time message on PATH_ADJUST_LIMIT. A message is not stored:
     * if the phone is not connected it is lost, so the connected nodes are checked first.
     * The local state is not updated: the phone saves the limit and answers with feature 1.
     */
    private fun sendNewLimit(value: Double) {
        // Wearable calls return a Task; they are awaited with await() in a coroutine
        // tied to the lifecycle so the main thread is never blocked.
        lifecycleScope.launch {
            try {
                // Nodes = devices paired and connected right now (the phone).
                val nodes = Wearable.getNodeClient(this@MainActivity).connectedNodes.await()
                if (nodes.isEmpty()) {
                    toast("Sin conexión con el teléfono")
                    return@launch
                }
                // The payload travels as bytes: the limit as UTF-8 text, for example "600.0".
                val payload = value.toString().toByteArray(Charsets.UTF_8)
                val messageClient = Wearable.getMessageClient(this@MainActivity)
                nodes.forEach { node ->
                    messageClient.sendMessage(node.id, WearableConstants.PATH_ADJUST_LIMIT, payload).await()
                }
                toast("Límite enviado")
                screen = Screen.GLANCE
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo enviar el límite", e)
                toast("Sin conexión con el teléfono")
            }
        }
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "HorosWear"
    }
}

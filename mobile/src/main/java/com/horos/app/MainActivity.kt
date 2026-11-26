package com.horos.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.horos.app.data.BudgetStorage
import com.horos.app.data.Expense
import com.horos.app.ui.screens.HomeScreen
import com.horos.app.ui.screens.RegisterExpenseScreen
import com.horos.app.ui.screens.formatMoney
import com.horos.app.ui.theme.HorosTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Main phone screen and the source of truth for Horos+:
 * it stores expenses, calculates the remaining budget and publishes it to the watch (feature 1).
 * It also listens for the limit adjustment sent by the watch (feature 2), which is why
 * it implements MessageClient.OnMessageReceivedListener.
 */
class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    private enum class Screen { HOME, REGISTER }

    private lateinit var storage: BudgetStorage

    // Compose-observable state: changing any of these values redraws the UI.
    private var limit by mutableDoubleStateOf(BudgetStorage.DEFAULT_LIMIT)
    private var spent by mutableDoubleStateOf(0.0)
    private var expenses by mutableStateOf(emptyList<Expense>())
    private var screen by mutableStateOf(Screen.HOME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        storage = BudgetStorage(this)
        refreshState()

        setContent {
            HorosTheme {
                when (screen) {
                    Screen.HOME -> HomeScreen(
                        limit = limit,
                        spent = spent,
                        expenses = expenses,
                        onRegisterClick = { screen = Screen.REGISTER }
                    )
                    Screen.REGISTER -> {
                        BackHandler { screen = Screen.HOME }
                        RegisterExpenseScreen(
                            onSave = ::saveExpense,
                            onBack = { screen = Screen.HOME }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Registers this listener on the MessageClient to receive messages from the watch.
        // It lives with the Activity: if the phone app is closed, the limit adjustment is lost.
        Wearable.getMessageClient(this).addListener(this)
        refreshState()
        // Re-sends the state when returning to the app; also covers a new day (spent goes back to 0).
        syncBudgetToWatch()
    }

    override fun onPause() {
        super.onPause()
        // Removes the listener so no messages arrive while the Activity is in the background.
        Wearable.getMessageClient(this).removeListener(this)
    }

    /**
     * Feature 2 (MessageClient, watch → phone).
     * The watch sends a message on PATH_ADJUST_LIMIT whose payload is the new limit
     * as UTF-8 text (for example "600.0"). Messages on any other path are ignored.
     */
    override fun onMessageReceived(event: MessageEvent) {
        if (event.path != WearableConstants.PATH_ADJUST_LIMIT) return

        // event.data holds the payload bytes; they are converted back to a number.
        val newLimit = String(event.data, Charsets.UTF_8).toDoubleOrNull()
        if (newLimit == null || newLimit <= 0) {
            Log.w(TAG, "Payload de límite inválido")
            return
        }

        // Makes sure the Compose state update and the Toast run on the main thread.
        runOnUiThread {
            storage.setDailyLimit(newLimit)
            refreshState()
            Toast.makeText(
                this,
                "Límite actualizado desde el reloj: ${formatMoney(newLimit)}",
                Toast.LENGTH_SHORT
            ).show()
            // The new limit changes the remaining budget: feature 1 runs so the watch sees it.
            syncBudgetToWatch()
        }
    }

    // Called from Register expense: saves, refreshes the UI, notifies the watch and returns Home.
    private fun saveExpense(expense: Expense) {
        storage.addExpense(expense)
        refreshState()
        syncBudgetToWatch()
        screen = Screen.HOME
    }

    /**
     * Feature 1 (DataClient, phone → watch).
     * Publishes the budget state as a DataItem on PATH_BUDGET. Unlike a message,
     * the DataItem stays stored in the Data Layer, so the watch finds it
     * even if it opens the app later.
     */
    private fun syncBudgetToWatch() {
        // PutDataMapRequest builds the DataItem: a path plus a map of keys and values.
        val request = PutDataMapRequest.create(WearableConstants.PATH_BUDGET).apply {
            dataMap.putDouble(WearableConstants.KEY_DAILY_LIMIT, limit)
            dataMap.putDouble(WearableConstants.KEY_SPENT_TODAY, spent)
            dataMap.putDouble(WearableConstants.KEY_REMAINING, limit - spent)
            // DataClient only notifies when the content changes; the timestamp guarantees it always does.
            dataMap.putLong(WearableConstants.KEY_UPDATED_AT, System.currentTimeMillis())
        }.asPutDataRequest()
            // Urgent: sent right away instead of waiting for the system to batch updates.
            .setUrgent()

        // putDataItem returns a Play Services Task. Inside lifecycleScope.launch it is awaited
        // with await() without blocking the main thread, and the coroutine is cancelled if the Activity is destroyed.
        lifecycleScope.launch {
            try {
                Wearable.getDataClient(this@MainActivity).putDataItem(request).await()
            } catch (e: Exception) {
                // Fails, for example, when Play Services is not available on the device.
                Log.e(TAG, "No se pudo sincronizar con el reloj", e)
            }
        }
    }

    // Reads BudgetStorage again; assigning the state makes Compose redraw the screen.
    private fun refreshState() {
        limit = storage.getDailyLimit()
        spent = storage.getSpentToday()
        expenses = storage.getExpensesToday()
    }

    companion object {
        private const val TAG = "HorosMobile"
    }
}

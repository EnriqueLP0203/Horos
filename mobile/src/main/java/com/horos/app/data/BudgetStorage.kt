package com.horos.app.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Single access point to SharedPreferences (file horos_prefs).
 * Stores the daily limit, today's spending and today's expense list as JSON.
 * The day's data resets automatically when the date changes.
 */
class BudgetStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDailyLimit(): Double = prefs.getDouble(KEY_LIMIT, DEFAULT_LIMIT)

    fun setDailyLimit(value: Double) {
        prefs.edit().putDouble(KEY_LIMIT, value).apply()
    }

    fun getSpentToday(): Double {
        resetIfNewDay()
        return prefs.getDouble(KEY_SPENT, 0.0)
    }

    // Can be negative when the limit has been exceeded.
    fun getRemaining(): Double = getDailyLimit() - getSpentToday()

    fun addExpense(expense: Expense) {
        resetIfNewDay()
        val list = readExpenses()
        list.put(expense.toJson())
        prefs.edit()
            .putDouble(KEY_SPENT, prefs.getDouble(KEY_SPENT, 0.0) + expense.amount)
            .putString(KEY_EXPENSES, list.toString())
            .apply()
    }

    fun getExpensesToday(): List<Expense> {
        resetIfNewDay()
        val list = readExpenses()
        return (0 until list.length()).map { Expense.fromJson(list.getJSONObject(it)) }
    }

    // If the stored date is not today, resets the spent amount to 0 and clears the list.
    fun resetIfNewDay() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (prefs.getString(KEY_DATE, null) == today) return
        prefs.edit()
            .putDouble(KEY_SPENT, 0.0)
            .putString(KEY_EXPENSES, "[]")
            .putString(KEY_DATE, today)
            .apply()
    }

    private fun readExpenses(): JSONArray =
        runCatching { JSONArray(prefs.getString(KEY_EXPENSES, "[]")) }.getOrDefault(JSONArray())

    // SharedPreferences has no Double type; the value is stored as its raw bits in a Long.
    private fun SharedPreferences.getDouble(key: String, default: Double) =
        Double.fromBits(getLong(key, default.toRawBits()))

    private fun SharedPreferences.Editor.putDouble(key: String, value: Double) =
        putLong(key, value.toRawBits())

    companion object {
        private const val PREFS_NAME = "horos_prefs"
        private const val KEY_LIMIT = "daily_limit"
        private const val KEY_SPENT = "spent_today"
        private const val KEY_EXPENSES = "expenses_today"
        private const val KEY_DATE = "last_date"
        const val DEFAULT_LIMIT = 500.0
    }
}

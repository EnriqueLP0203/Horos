package com.horos.app.data

import org.json.JSONObject

// A single expense of the day. Stored as JSON in SharedPreferences; never sent to the watch.
data class Expense(
    val amount: Double,
    val category: String,
    val note: String,
    val time: String // HH:mm
) {
    fun toJson(): JSONObject = JSONObject()
        .put("amount", amount)
        .put("category", category)
        .put("note", note)
        .put("time", time)

    companion object {
        fun fromJson(json: JSONObject) = Expense(
            amount = json.optDouble("amount", 0.0),
            category = json.optString("category"),
            note = json.optString("note"),
            time = json.optString("time")
        )
    }
}

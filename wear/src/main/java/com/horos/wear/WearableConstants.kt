package com.horos.wear

/**
 * Data Layer paths and keys shared by the phone and the watch.
 * Must hold exactly the same values as WearableConstants in the mobile module;
 * if they differ, data and messages never arrive and no error is shown.
 */
object WearableConstants {
    // Feature 1 (DataClient, phone → watch): DataItem path and its keys.
    const val PATH_BUDGET = "/horos/budget"
    const val KEY_DAILY_LIMIT = "daily_limit"
    const val KEY_SPENT_TODAY = "spent_today"
    const val KEY_REMAINING = "remaining"
    // Send time in milliseconds; makes every DataItem different even if the amounts repeat.
    const val KEY_UPDATED_AT = "updated_at"

    // Feature 2 (MessageClient, watch → phone): the payload is the new limit as UTF-8 text.
    const val PATH_ADJUST_LIMIT = "/horos/adjust_limit"
}

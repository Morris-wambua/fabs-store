package com.morrislabs.fabs_store.util

import android.text.format.DateUtils
import com.morrislabs.fabs_store.localization.MeasurementFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimeAgo(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Just now"

    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
        val date = parser.parse(dateString) ?: return "Just now"
        DateUtils.getRelativeTimeSpanString(
            date.time,
            Date().time,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    } catch (e: Exception) {
        "Unknown date"
    }
}

fun formatDuration(minutes: Int?): String {
    val safeMinutes = minutes ?: 0
    return MeasurementFormatter.formatDurationMinutes(safeMinutes, Locale.getDefault())
}

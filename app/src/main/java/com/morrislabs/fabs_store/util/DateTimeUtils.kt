package com.morrislabs.fabs_store.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatTimeAgo(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Just now"

    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val date = format.parse(dateString) ?: return "Just now"
        val now = Date()
        val diffInMillis = now.time - date.time

        when {
            diffInMillis < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diffInMillis < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
                "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            }
            diffInMillis < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
                "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            }
            diffInMillis < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                "$days ${if (days == 1L) "day" else "days"} ago"
            }
            diffInMillis < TimeUnit.DAYS.toMillis(30) -> {
                val weeks = TimeUnit.MILLISECONDS.toDays(diffInMillis) / 7
                "$weeks ${if (weeks == 1L) "week" else "weeks"} ago"
            }
            else -> {
                val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                dateFormat.format(date)
            }
        }
    } catch (e: Exception) {
        "Unknown date"
    }
}

fun formatDuration(minutes: Int?): String {
    if (minutes == null) return "0 min"

    return if (minutes < 60) {
        "$minutes min"
    } else {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        if (remainingMinutes == 0) {
            "$hours hr"
        } else {
            "$hours hr $remainingMinutes min"
        }
    }
}

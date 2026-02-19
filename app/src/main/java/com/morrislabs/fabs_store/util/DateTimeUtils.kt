package com.morrislabs.fabs_store.util

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

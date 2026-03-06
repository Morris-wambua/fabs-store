package com.morrislabs.fabs_store.localization

import java.text.NumberFormat
import java.util.Locale

object MeasurementFormatter {
    private val mileRegions = setOf("US", "GB", "LR", "MM")

    fun formatDistanceKilometers(kilometers: Double, locale: Locale): String {
        val formatter = NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = 1
            minimumFractionDigits = 0
        }

        return if (mileRegions.contains(locale.country.uppercase(Locale.US))) {
            val miles = kilometers * 0.621371
            "${formatter.format(miles)} mi"
        } else {
            "${formatter.format(kilometers)} km"
        }
    }

    fun formatDurationMinutes(minutes: Int, locale: Locale): String {
        val formatter = NumberFormat.getIntegerInstance(locale)
        return "${formatter.format(minutes)} min"
    }
}



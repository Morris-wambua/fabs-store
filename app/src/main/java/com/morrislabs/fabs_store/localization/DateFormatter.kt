package com.morrislabs.fabs_store.localization

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object DateFormatter {
    fun formatDate(date: LocalDate, locale: Locale, style: FormatStyle = FormatStyle.MEDIUM): String {
        return date.format(DateTimeFormatter.ofLocalizedDate(style).withLocale(locale))
    }

    fun formatTime(time: LocalTime, locale: Locale, style: FormatStyle = FormatStyle.SHORT): String {
        return time.format(DateTimeFormatter.ofLocalizedTime(style).withLocale(locale))
    }
}



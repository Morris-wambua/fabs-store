package com.morrislabs.fabs_store.localization

import android.content.Context
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {
    fun currencySymbol(locale: Locale): String {
        return Currency.getInstance(locale).symbol
    }

    fun format(amount: Number, context: Context): String {
        ExchangeRateManager.initialize(context)
        return format(amount, LocaleManager.getActiveLocale(context))
    }

    fun format(amount: Number, locale: Locale): String {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = Currency.getInstance(locale)
        val converted = ExchangeRateManager.convertUsdToLocale(amount, locale)
        return formatter.format(converted)
    }

    fun formatWithCurrencyCode(amount: Number, currencyCode: String, locale: Locale): String {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = Currency.getInstance(currencyCode)
        return formatter.format(amount)
    }
}


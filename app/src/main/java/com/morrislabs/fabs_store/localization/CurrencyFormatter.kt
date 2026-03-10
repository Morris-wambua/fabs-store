package com.morrislabs.fabs_store.localization

import android.content.Context
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {
    fun currencySymbol(locale: Locale): String {
        return resolveCurrency(locale).symbol
    }

    fun format(amount: Number, context: Context): String {
        return format(amount, LocaleManager.getActiveLocale(context))
    }

    fun format(amount: Number, locale: Locale): String {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = resolveCurrency(locale)
        return formatter.format(amount)
    }

    fun formatWithCurrencyCode(amount: Number, currencyCode: String, locale: Locale): String {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        formatter.currency = runCatching { Currency.getInstance(currencyCode) }
            .getOrElse { Currency.getInstance("USD") }
        return formatter.format(amount)
    }

    fun formatAmountFromCurrencyCode(amount: Number, sourceCurrencyCode: String, locale: Locale): String {
        val currency = runCatching { Currency.getInstance(sourceCurrencyCode) }
            .getOrElse { resolveCurrency(locale) }
        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            this.currency = currency
        }
        return formatter.format(amount)
    }

    fun formatNative(amount: Number, currencyCode: String): String {
        val currency = runCatching { Currency.getInstance(currencyCode) }
            .getOrElse { Currency.getInstance("USD") }
        val locale = currencyToLocale(currencyCode)
        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            this.currency = currency
        }
        return formatter.format(amount)
    }

    private fun currencyToLocale(currencyCode: String): Locale {
        return when (currencyCode.uppercase()) {
            "KES" -> Locale("en", "KE")
            "USD" -> Locale.US
            "GBP" -> Locale.UK
            "EUR" -> Locale.GERMANY
            "NGN" -> Locale("en", "NG")
            "CNY" -> Locale.CHINA
            "JPY" -> Locale.JAPAN
            "INR" -> Locale("en", "IN")
            "AUD" -> Locale("en", "AU")
            "CAD" -> Locale.CANADA
            "GHS" -> Locale("en", "GH")
            "TZS" -> Locale("en", "TZ")
            "UGX" -> Locale("en", "UG")
            "ZAR" -> Locale("en", "ZA")
            "BRL" -> Locale("pt", "BR")
            "AED" -> Locale("ar", "AE")
            else -> Locale.US
        }
    }

    private fun resolveCurrency(locale: Locale): Currency {
        return runCatching { Currency.getInstance(locale) }
            .getOrElse { Currency.getInstance("USD") }
    }
}


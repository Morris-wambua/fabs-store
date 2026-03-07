package com.morrislabs.fabs_store.localization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class LocalizationUtilsTest {

    @Test
    fun `currency formatter returns non blank values`() {
        ExchangeRateManager.clearRatesForTesting()
        val us = CurrencyFormatter.format(1234.5, Locale.US)
        val japan = CurrencyFormatter.format(1234.5, Locale.JAPAN)

        assertTrue(us.isNotBlank())
        assertTrue(japan.isNotBlank())
    }

    @Test
    fun `distance formatter switches to miles for US`() {
        val usDistance = MeasurementFormatter.formatDistanceKilometers(10.0, Locale.US)
        val frDistance = MeasurementFormatter.formatDistanceKilometers(10.0, Locale.FRANCE)

        assertTrue(usDistance.endsWith("mi"))
        assertTrue(frDistance.endsWith("km"))
    }

    @Test
    fun `default calling code follows locale region`() {
        assertEquals("+1", PhoneNumberFormatter.defaultCallingCode(Locale.US))
        assertEquals("+44", PhoneNumberFormatter.defaultCallingCode(Locale.UK))
    }

    @Test
    fun `date formatter is locale aware`() {
        val date = LocalDate.of(2026, 3, 6)
        val us = DateFormatter.formatDate(date, Locale.US)
        val gb = DateFormatter.formatDate(date, Locale.UK)

        assertTrue(us != gb)
    }

    @Test
    fun `currency formatter converts usd amount to locale currency`() {
        ExchangeRateManager.setRatesForTesting(
            mapOf(
                "KES" to 130.0,
                "USD" to 1.0
            )
        )

        val kenyaLocale = Locale.Builder().setLanguage("en").setRegion("KE").build()
        val kenya = CurrencyFormatter.format(1.00, kenyaLocale)
        assertTrue(kenya.contains("130"))
    }
}

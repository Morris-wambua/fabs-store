package com.morrislabs.fabs_store.localization

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

data class CountryPhoneEntry(
    val regionCode: String,
    val callingCode: String,
    val displayName: String,
    val flagEmoji: String
)

object PhoneNumberFormatter {
    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    fun defaultCallingCode(locale: Locale): String {
        val region = regionForLocale(locale)
        val callingCode = phoneUtil.getCountryCodeForRegion(region)
        return if (callingCode > 0) "+$callingCode" else "+1"
    }

    fun toE164(rawPhone: String, locale: Locale): String? {
        return try {
            val number = phoneUtil.parse(rawPhone, regionForLocale(locale))
            if (!phoneUtil.isValidNumber(number)) return null
            phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (_: NumberParseException) {
            null
        }
    }

    fun formatInternational(rawPhone: String, locale: Locale): String {
        return try {
            val number = phoneUtil.parse(rawPhone, regionForLocale(locale))
            phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
        } catch (_: NumberParseException) {
            rawPhone
        }
    }

    fun isValid(rawPhone: String, locale: Locale): Boolean {
        return try {
            val number = phoneUtil.parse(rawPhone, regionForLocale(locale))
            phoneUtil.isValidNumber(number)
        } catch (_: NumberParseException) {
            false
        }
    }

    fun defaultCountryEntry(locale: Locale): CountryPhoneEntry {
        val region = regionForLocale(locale)
        val localizedName = Locale("", region).getDisplayCountry(locale).ifBlank { region }
        return CountryPhoneEntry(
            regionCode = region,
            callingCode = defaultCallingCode(locale),
            displayName = localizedName,
            flagEmoji = regionToFlagEmoji(region)
        )
    }

    fun supportedCountries(locale: Locale): List<CountryPhoneEntry> {
        return phoneUtil.supportedRegions
            .map { region ->
                val callingCode = phoneUtil.getCountryCodeForRegion(region)
                CountryPhoneEntry(
                    regionCode = region,
                    callingCode = if (callingCode > 0) "+$callingCode" else "",
                    displayName = Locale("", region).getDisplayCountry(locale).ifBlank { region },
                    flagEmoji = regionToFlagEmoji(region)
                )
            }
            .filter { it.callingCode.isNotBlank() }
            .sortedBy { it.displayName.lowercase(locale) }
    }

    private fun regionForLocale(locale: Locale): String {
        return locale.country.takeIf { it.length == 2 }?.uppercase(Locale.US) ?: "US"
    }

    private fun regionToFlagEmoji(regionCode: String): String {
        if (regionCode.length != 2) return ""
        val first = Character.codePointAt(regionCode, 0) - 0x41 + 0x1F1E6
        val second = Character.codePointAt(regionCode, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(first)) + String(Character.toChars(second))
    }
}



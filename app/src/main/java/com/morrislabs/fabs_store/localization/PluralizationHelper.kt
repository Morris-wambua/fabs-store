package com.morrislabs.fabs_store.localization

import android.content.res.Resources
import androidx.annotation.PluralsRes

object PluralizationHelper {
    fun quantityString(
        resources: Resources,
        @PluralsRes pluralResId: Int,
        quantity: Int,
        vararg formatArgs: Any
    ): String {
        return resources.getQuantityString(pluralResId, quantity, *formatArgs)
    }
}



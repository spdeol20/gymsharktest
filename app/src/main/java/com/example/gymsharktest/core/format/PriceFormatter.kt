package com.example.gymsharktest.core.format

import com.example.gymsharktest.model.Price
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Renders [Price] amounts, which the catalogue supplies in minor units.
 *
 * The payload has no currency field, so GBP is assumed; both the currency and locale are
 * constructor parameters so that assumption is visible and overridable rather than buried.
 */
class PriceFormatter(
    locale: Locale = Locale.UK,
    private val currency: Currency = Currency.getInstance("GBP"),
) {
    // NumberFormat is not thread-safe, so every use is guarded.
    private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(locale).also {
        it.currency = currency
        it.minimumFractionDigits = currency.defaultFractionDigits
        it.maximumFractionDigits = currency.defaultFractionDigits
    }

    fun format(minorUnits: Long): String = synchronized(numberFormat) {
        numberFormat.format(toMajorUnits(minorUnits))
    }

    fun format(price: Price): String = format(price.amountMinorUnits)

    /** The struck-through "was" price, or null when the product is not discounted. */
    fun formatCompareAt(price: Price): String? {
        if (!price.isDiscounted) return null
        val compareAt = price.compareAtMinorUnits ?: return null
        return format(compareAt)
    }

    private fun toMajorUnits(minorUnits: Long): BigDecimal =
        BigDecimal.valueOf(minorUnits).movePointLeft(currency.defaultFractionDigits.coerceAtLeast(0))
}

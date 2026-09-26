package com.example.gymsharktest.model

import androidx.compose.runtime.Immutable
import kotlin.math.roundToInt

/**
 * A product price.
 *
 * Amounts are in **minor units** exactly as the catalogue API returns them, so `1000` is 10.00.
 * The payload carries no currency code; rendering assumes GBP (see `PriceFormatter`).
 */
@Immutable
data class Price(
    val amountMinorUnits: Long,
    val compareAtMinorUnits: Long? = null,
) {
    val isDiscounted: Boolean
        get() = compareAtMinorUnits != null && compareAtMinorUnits > amountMinorUnits

    /**
     * Derived rather than taken from the payload's `discountPercentage` field, so the percentage
     * shown can never disagree with the two prices shown beside it.
     */
    val discountPercent: Int?
        get() {
            val compareAt = compareAtMinorUnits ?: return null
            if (compareAt <= 0L || compareAt <= amountMinorUnits) return null
            val saving = (compareAt - amountMinorUnits).toDouble() / compareAt.toDouble()
            return (saving * 100).roundToInt().coerceIn(1, 99)
        }
}

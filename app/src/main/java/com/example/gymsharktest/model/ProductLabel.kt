package com.example.gymsharktest.model

import androidx.compose.runtime.Immutable

/**
 * A merchandising label shown as a badge on a product.
 *
 * The label vocabulary is an open set: the catalogue can introduce new values at any time without
 * a client release. Unrecognised values are therefore preserved as [Unknown] and still rendered,
 * rather than being dropped, which would silently hide merchandising the API asked us to show.
 */
@Immutable
sealed interface ProductLabel {

    data object New : ProductLabel

    data object ComingSoon : ProductLabel

    data object BackInStock : ProductLabel

    data object Sale : ProductLabel

    @Immutable
    data class Unknown(val text: String) : ProductLabel

    companion object {

        /** Longest label text rendered for an unrecognised value. */
        const val MAX_UNKNOWN_LENGTH: Int = 24

        private val WHITESPACE = Regex("[\\s\\u00A0]+")

        /**
         * Returns null for blank input so callers can map a whole list and drop the nulls.
         *
         * Whitespace is collapsed before control characters are stripped, otherwise a newline
         * between two words would silently join them.
         */
        fun from(raw: String?): ProductLabel? {
            val normalised = raw
                ?.replace(WHITESPACE, " ")
                ?.filter { !it.isISOControl() }
                ?.trim()
                .orEmpty()
            if (normalised.isEmpty()) return null

            return when (normalised.lowercase().replace('_', ' ').replace('-', ' ')) {
                "new", "new in", "newin", "new arrival", "new arrivals" -> New
                "coming soon", "comingsoon", "pre order", "preorder" -> ComingSoon
                "back in stock", "backinstock", "restocked" -> BackInStock
                "sale", "on sale", "discount", "discounted" -> Sale
                else -> Unknown(capLength(normalised))
            }
        }

        /** Unknown labels are untrusted display text, so a hostile value cannot break the badge. */
        private fun capLength(text: String): String = if (text.length <= MAX_UNKNOWN_LENGTH) {
            text
        } else {
            text.take(MAX_UNKNOWN_LENGTH - 1).trimEnd() + "\u2026"
        }
    }
}

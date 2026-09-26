package com.example.gymsharktest.model

import androidx.compose.runtime.Immutable

/**
 * A merchandising or material label shown as a badge on a product.
 *
 * The label vocabulary is an open set: the catalogue can introduce new values at any time without
 * a client release. Unrecognised values are therefore preserved as [Unknown] and still rendered,
 * rather than being dropped, which would silently hide merchandising the API asked us to show.
 *
 * [Kind] splits product-state labels from material attributes so the card can show one decision
 * signal and the detail screen can put recycled-fibre claims next to the description.
 */
@Immutable
sealed interface ProductLabel {

    val kind: Kind
        get() = when (this) {
            RecycledNylon, RecycledPolyester -> Kind.Material
            else -> Kind.Merchandising
        }

    enum class Kind { Merchandising, Material }

    data object New : ProductLabel

    data object ComingSoon : ProductLabel

    data object BackInStock : ProductLabel

    data object Sale : ProductLabel

    data object GoingFast : ProductLabel

    data object LimitedEdition : ProductLabel

    data object Popular : ProductLabel

    data object RecycledNylon : ProductLabel

    data object RecycledPolyester : ProductLabel

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

            val forMatch = normalised.lowercase().replace('_', ' ').replace('-', ' ')
            return when (forMatch) {
                "new", "new in", "newin", "new arrival", "new arrivals" -> New
                "coming soon", "comingsoon", "pre order", "preorder" -> ComingSoon
                "back in stock", "backinstock", "restocked" -> BackInStock
                "sale", "on sale", "discount", "discounted" -> Sale
                "going fast", "goingfast" -> GoingFast
                "limited edition", "limitededition" -> LimitedEdition
                "popular" -> Popular
                "recycled nylon", "recyclednylon" -> RecycledNylon
                "recycled polyester", "recycledpolyester" -> RecycledPolyester
                else -> Unknown(capLength(normalised.replace('_', ' ').replace('-', ' ')))
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

package com.example.gymsharktest.ui.products

import com.example.gymsharktest.model.Product

/** How the shop grid is ordered. The featured carousel is never sorted by this. */
enum class CatalogueSort {
    /** The order the catalogue response arrived in. */
    Catalogue,

    PriceLowToHigh,

    PriceHighToLow,
}

/**
 * Equal prices keep their original relative order. [sortedBy] and [sortedByDescending] are stable.
 */
fun List<Product>.sortedFor(sort: CatalogueSort): List<Product> = when (sort) {
    CatalogueSort.Catalogue -> this
    CatalogueSort.PriceLowToHigh -> sortedBy { it.price.amountMinorUnits }
    CatalogueSort.PriceHighToLow -> sortedByDescending { it.price.amountMinorUnits }
}

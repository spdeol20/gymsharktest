package com.example.gymsharktest.ui.products

import com.example.gymsharktest.model.Product
import com.example.gymsharktest.model.ProductLabel

/**
 * Merchandising labels worth a chip, in the order a shopper should meet them.
 * Material labels stay off this row; they are attributes, not a reason to feature a product.
 */
private val CHIP_ORDER = listOf(
    ProductLabel.New,
    ProductLabel.LimitedEdition,
    ProductLabel.GoingFast,
    ProductLabel.Popular,
    ProductLabel.ComingSoon,
    ProductLabel.BackInStock,
    ProductLabel.Sale,
)

/** Stable id for the selected chip across rotation. Null means the All chip. */
fun ProductLabel.shelfKey(): String = when (this) {
    ProductLabel.New -> "new"
    ProductLabel.LimitedEdition -> "limited-edition"
    ProductLabel.GoingFast -> "going-fast"
    ProductLabel.Popular -> "popular"
    ProductLabel.ComingSoon -> "coming-soon"
    ProductLabel.BackInStock -> "back-in-stock"
    ProductLabel.Sale -> "sale"
    ProductLabel.RecycledNylon -> "recycled-nylon"
    ProductLabel.RecycledPolyester -> "recycled-polyester"
    is ProductLabel.Unknown -> "unknown:$text"
}

/**
 * Chips that have at least one in-stock product. An empty list means the shelf should not render.
 */
fun featuredLabels(products: List<Product>): List<ProductLabel> {
    val present = products
        .asSequence()
        .filter { it.inStock }
        .flatMap { it.merchandisingLabels.asSequence() }
        .distinct()
        .toList()
    if (present.isEmpty()) return emptyList()

    val ordered = CHIP_ORDER.filter { it in present }
    val extras = present
        .filter { it !in CHIP_ORDER }
        .sortedBy { label -> if (label is ProductLabel.Unknown) label.text.lowercase() else label.shelfKey() }
    return ordered + extras
}

/** How many cards the featured row shows. The grid below carries the rest. */
const val FEATURED_ROW_LIMIT = 5

/**
 * The first [FEATURED_ROW_LIMIT] in-stock products for the shelf.
 * A null label is the All chip: any merchandising label qualifies.
 */
fun featuredProducts(products: List<Product>, label: ProductLabel?): List<Product> =
    products.filter { product ->
        product.inStock && if (label == null) {
            product.merchandisingLabels.isNotEmpty()
        } else {
            label in product.merchandisingLabels
        }
    }.take(FEATURED_ROW_LIMIT)

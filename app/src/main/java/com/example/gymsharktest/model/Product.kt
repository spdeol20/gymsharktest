package com.example.gymsharktest.model

import androidx.compose.runtime.Immutable

/**
 * A catalogue product, already validated: every field the UI reads is either non-null or
 * explicitly optional, so no screen has to defend against a malformed payload.
 */
@Immutable
data class Product(
    val id: Long,
    val sku: String,
    val title: String,
    val colour: String?,
    val type: String?,
    val descriptionHtml: String,
    val price: Price,
    val inStock: Boolean,
    val labels: List<ProductLabel> = emptyList(),
    val images: List<ProductImage> = emptyList(),
    val sizes: List<SizeAvailability> = emptyList(),
) {
    val featuredImage: ProductImage?
        get() = images.firstOrNull()

    val hasImage: Boolean
        get() = images.isNotEmpty()

    val availableSizes: List<SizeAvailability>
        get() = sizes.filter { it.inStock }

    val merchandisingLabels: List<ProductLabel>
        get() = labels.filter { it.kind == ProductLabel.Kind.Merchandising }

    val materialLabels: List<ProductLabel>
        get() = labels.filter { it.kind == ProductLabel.Kind.Material }

    val remainingSizeCount: Int
        get() = availableSizes.size

    /**
     * True when the product is buyable but only one or two sizes remain. The threshold is two
     * because that is where the payload's own `going-fast` label typically appears, and a count
     * is more honest than the marketing copy.
     */
    val isLowStock: Boolean
        get() = inStock && sizes.isNotEmpty() && remainingSizeCount in 1..LOW_STOCK_SIZE_LIMIT

    /**
     * Discount shown on the card. Sold-out products suppress it so an unbuyable item does not
     * also shout a sale.
     */
    val cardDiscountPercent: Int?
        get() = if (inStock) price.discountPercent else null

    /**
     * The single merchandising label a card may show. Discount takes the overlay first; sold-out
     * products show no label at all.
     */
    val cardLabel: ProductLabel?
        get() = if (inStock && cardDiscountPercent == null) merchandisingLabels.firstOrNull() else null

    companion object {
        const val LOW_STOCK_SIZE_LIMIT: Int = 2
    }
}

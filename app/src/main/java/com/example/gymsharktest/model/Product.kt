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
}

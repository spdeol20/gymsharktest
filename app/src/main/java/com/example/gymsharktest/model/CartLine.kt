package com.example.gymsharktest.model

import androidx.compose.runtime.Immutable

/**
 * One basket line. [size] is null when the product has no sizes.
 *
 * The line stores a product id, a size, and a quantity. It is not a payment.
 */
@Immutable
data class CartLine(
    val product: Product,
    val size: String?,
    val quantity: Int,
)

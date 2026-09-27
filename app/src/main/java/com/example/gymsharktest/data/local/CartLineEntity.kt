package com.example.gymsharktest.data.local

import androidx.room.Entity

/**
 * A basket selection. The key is the product plus the size, so the same product in two sizes is
 * two rows. [sizeKey] is empty when the product has no sizes.
 *
 * This is not payment data. There is no card number, name, or address.
 */
@Entity(
    tableName = "cart_lines",
    primaryKeys = ["productId", "sizeKey"],
)
data class CartLineEntity(
    val productId: Long,
    val sizeKey: String,
    val quantity: Int,
    val addedAt: Long,
)

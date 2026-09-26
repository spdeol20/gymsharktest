package com.example.gymsharktest.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One catalogue row. Nested media, sizes, and labels are JSON because they are always read with
 * the product and never queried on their own.
 *
 * This table is a cache of a public catalogue. It holds no account data, tokens, or other PII.
 */
@Entity(
    tableName = "products",
    indices = [
        Index(value = ["position"]),
        Index(value = ["amountMinorUnits", "position"]),
        Index(value = ["inStock", "hasMerchandisingLabel", "position"]),
    ],
)
data class ProductEntity(
    @PrimaryKey val id: Long,
    val sku: String,
    val title: String,
    val colour: String?,
    val type: String?,
    val descriptionHtml: String,
    val amountMinorUnits: Long,
    val compareAtMinorUnits: Long?,
    val inStock: Boolean,
    val labelsJson: String,
    val imagesJson: String,
    val sizesJson: String,
    val hasMerchandisingLabel: Boolean,
    /** Index in the last successful catalogue response. Ties in a price sort keep this order. */
    val position: Int,
)

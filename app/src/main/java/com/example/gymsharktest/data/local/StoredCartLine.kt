package com.example.gymsharktest.data.local

import androidx.room.Embedded
import androidx.room.Relation

/** A basket row joined to the catalogue product it points at. */
data class StoredCartLine(
    @Embedded val line: CartLineEntity,
    @Relation(parentColumn = "productId", entityColumn = "id")
    val product: ProductEntity?,
)

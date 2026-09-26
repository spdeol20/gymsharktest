package com.example.gymsharktest.data.local

import kotlinx.serialization.Serializable

/** JSON columns. The sealed UI types are not stored directly, so a new label cannot break a row. */
@Serializable
internal data class StoredImage(
    val url: String,
    val widthPx: Int? = null,
    val heightPx: Int? = null,
)

@Serializable
internal data class StoredSize(
    val size: String,
    val inStock: Boolean,
)

@Serializable
internal data class StoredLabel(
    val key: String,
    val text: String? = null,
)

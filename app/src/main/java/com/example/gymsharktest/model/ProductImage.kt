package com.example.gymsharktest.model

import androidx.compose.runtime.Immutable

/**
 * An image that has already been validated as safe to load: [url] is always an absolute HTTPS URL.
 * Products whose media fails validation carry no [ProductImage] at all, so the UI only ever has to
 * distinguish "has an image" from "has none".
 */
@Immutable
data class ProductImage(
    val url: String,
    val widthPx: Int? = null,
    val heightPx: Int? = null,
) {
    val aspectRatio: Float?
        get() {
            val width = widthPx ?: return null
            val height = heightPx ?: return null
            return if (width > 0 && height > 0) width.toFloat() / height.toFloat() else null
        }
}

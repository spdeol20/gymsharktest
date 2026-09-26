package com.example.gymsharktest.core.image

import java.net.URI

/**
 * Accepts an image URL only if it is absolute, HTTPS, and has a host.
 *
 * Returning null rather than the original string means an unusable URL never reaches the image
 * loader, so the UI only has to distinguish "has an image" from "has none" instead of also
 * handling "has a string that will fail to load".
 */
fun validatedImageUrl(raw: String?): String? {
    val trimmed = raw?.trim().orEmpty()
    if (trimmed.isEmpty()) return null

    // URI.create wraps the checked URISyntaxException, which Kotlin would otherwise let escape.
    val uri = try {
        URI.create(trimmed)
    } catch (_: IllegalArgumentException) {
        return null
    }

    if (!uri.isAbsolute) return null
    if (!"https".equals(uri.scheme, ignoreCase = true)) return null
    if (uri.host.isNullOrBlank()) return null

    return trimmed
}

/**
 * Asks the CDN for a rendition at [widthPx] instead of the original file.
 *
 * The catalogue's images are 1692x2018; requesting ~400px renditions for a two-column grid is the
 * single largest bandwidth and decode saving in the app. Shopify's CDN reads this from the `width`
 * query parameter, and ignores it elsewhere, so an unrecognised host simply gets the original.
 */
fun String.withCdnWidth(widthPx: Int): String {
    if (widthPx <= 0 || isBlank()) return this
    if (contains("width=", ignoreCase = true)) return this

    val separator = if (contains('?')) '&' else '?'
    return "$this$separator" + "width=$widthPx"
}

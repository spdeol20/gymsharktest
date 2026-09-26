package com.example.gymsharktest.core.image

/**
 * A colour worth tinting the page with. Near-white studio backgrounds and near-black garments are
 * left out, so a wash is only returned when the photo actually has a colour.
 */
data class WashColor(
    val red: Int,
    val green: Int,
    val blue: Int,
)

/**
 * Averages the pixels that are neither a studio background nor a near-black garment.
 *
 * Returns null when there is not enough colour to tint the page. [pixels] are packed ARGB.
 */
fun sampleWashColor(pixels: IntArray): WashColor? {
    if (pixels.isEmpty()) return null

    var red = 0L
    var green = 0L
    var blue = 0L
    var count = 0
    for (pixel in pixels) {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        if (max == 0) continue
        val saturation = (max - min).toFloat() / max.toFloat()
        val luminance = (0.2126f * r + 0.7152f * g + 0.0722f * b) / 255f
        if (saturation < MIN_SATURATION) continue
        if (luminance < MIN_LUMINANCE || luminance > MAX_LUMINANCE) continue
        red += r
        green += g
        blue += b
        count++
    }
    if (count * 100 < pixels.size * MIN_COLOUR_PERCENT) return null
    return WashColor(
        red = (red / count).toInt(),
        green = (green / count).toInt(),
        blue = (blue / count).toInt(),
    )
}

private const val MIN_SATURATION = 0.22f
private const val MIN_LUMINANCE = 0.12f
private const val MAX_LUMINANCE = 0.92f
private const val MIN_COLOUR_PERCENT = 6

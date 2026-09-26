package com.example.gymsharktest.ui.detail

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.example.gymsharktest.core.image.WashColor
import com.example.gymsharktest.core.image.sampleWashColor
import com.example.gymsharktest.core.image.withCdnWidth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val SAMPLE_PX = 48
private const val CACHE_SIZE = 24

private val cacheLock = Any()
private val cache = object : LinkedHashMap<String, WashColor?>(CACHE_SIZE, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, WashColor?>?): Boolean =
        size > CACHE_SIZE
}

/**
 * The colour of the first product photo, remembered for this URL. Null until it loads, and null
 * for good when the photo has no colour worth showing or the load fails.
 */
@Composable
fun rememberImageWash(url: String?): WashColor? {
    if (url.isNullOrBlank()) return null
    val context = LocalContext.current
    var wash by remember(url) {
        mutableStateOf(
            when (val cached = cachedWash(url)) {
                WashLookup.Missing -> null
                is WashLookup.Hit -> cached.color
            },
        )
    }
    LaunchedEffect(url) {
        when (val cached = cachedWash(url)) {
            is WashLookup.Hit -> {
                wash = cached.color
                return@LaunchedEffect
            }
            WashLookup.Missing -> Unit
        }
        val sampled = withContext(Dispatchers.IO) { loadWash(context, url) }
        storeWash(url, sampled)
        wash = sampled
    }
    return wash
}

fun imageWashBrush(color: Color, sideBySide: Boolean): Brush = if (sideBySide) {
    Brush.horizontalGradient(
        0f to color,
        0.72f to Color.Transparent,
    )
} else {
    Brush.verticalGradient(
        0f to color,
        0.62f to Color.Transparent,
    )
}

fun WashColor.toComposeColor(): Color = Color(red, green, blue)

private fun cachedWash(url: String): WashLookup = synchronized(cacheLock) {
    if (!cache.containsKey(url)) WashLookup.Missing else WashLookup.Hit(cache[url])
}

private fun storeWash(url: String, color: WashColor?) {
    synchronized(cacheLock) { cache[url] = color }
}

private suspend fun loadWash(context: Context, url: String): WashColor? {
    val request = ImageRequest.Builder(context)
        .data(url.withCdnWidth(SAMPLE_PX))
        .size(SAMPLE_PX, SAMPLE_PX)
        .allowHardware(false)
        .build()
    val result = SingletonImageLoader.get(context).execute(request)
    val image = (result as? SuccessResult)?.image ?: return null
    val bitmap = image.toBitmap()
    val pixels = bitmap.copyPixels() ?: return null
    return sampleWashColor(pixels)
}

private fun Bitmap.copyPixels(): IntArray? = try {
    if (width <= 0 || height <= 0) return null
    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)
    pixels
} catch (_: IllegalStateException) {
    null
}

private sealed interface WashLookup {
    data object Missing : WashLookup
    data class Hit(val color: WashColor?) : WashLookup
}

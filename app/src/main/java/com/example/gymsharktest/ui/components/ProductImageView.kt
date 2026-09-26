package com.example.gymsharktest.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.SubcomposeAsyncImage
import com.example.gymsharktest.core.image.withCdnWidth
import com.example.gymsharktest.model.ProductImage

/**
 * Renders product media with three distinct outcomes, which is what the brief's "incorrect and/or
 * missing images" requirement actually asks for:
 *
 *  - no image at all: a labelled placeholder, no network request
 *  - image still loading: a shimmer of the final size, so the grid never reflows
 *  - image failed to load: the same placeholder, because a broken URL and a missing URL are the
 *    same thing to the user
 */
@Composable
fun ProductImageView(
    image: ProductImage?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    targetWidthPx: Int = DEFAULT_TARGET_WIDTH_PX,
    fallbackLabel: String? = null,
) {
    if (image == null) {
        ImagePlaceholder(
            modifier = modifier,
            fallbackLabel = fallbackLabel,
            contentDescription = contentDescription,
        )
        return
    }

    SubcomposeAsyncImage(
        model = image.url.withCdnWidth(targetWidthPx),
        contentDescription = contentDescription,
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentScale = ContentScale.Crop,
        loading = { ShimmerBox(modifier = Modifier.fillMaxSize()) },
        error = {
            ImagePlaceholder(
                modifier = Modifier.fillMaxSize(),
                fallbackLabel = fallbackLabel,
            )
        },
    )
}

@Composable
fun ImagePlaceholder(
    modifier: Modifier = Modifier,
    fallbackLabel: String? = null,
    contentDescription: String? = null,
) {
    val initial = fallbackLabel
        ?.trim()
        ?.firstOrNull { it.isLetterOrDigit() }
        ?.uppercaseChar()
        ?.toString()
        .orEmpty()

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (initial.isNotEmpty()) {
            Text(
                text = initial,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer-alpha",
    )

    Box(modifier = modifier.alpha(alpha).background(color))
}

/**
 * Wide enough for a grid cell on a high-density phone without pulling full-resolution media over
 * the network for every one of a thousand products.
 */
private const val DEFAULT_TARGET_WIDTH_PX = 600

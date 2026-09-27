package com.example.gymsharktest.ui.detail

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.example.gymsharktest.model.ProductImage
import com.example.gymsharktest.ui.components.ProductImageView
import kotlin.math.roundToInt

private const val FLIGHT_MILLIS = 480
private const val FLIGHT_END_SCALE = 0.15f
private const val FADE_START = 0.82f

/** Where the flying copy starts and where the bag icon is, both in window coordinates. */
data class FlyTarget(
    val image: ProductImage,
    val from: Rect,
    val to: Rect,
)

/**
 * A copy of the product photo that shrinks and travels into the bag. It is not announced, because
 * the bag badge is the confirmation a screen reader needs.
 */
@Composable
fun FlyToBagOverlay(
    target: FlyTarget,
    rootInWindow: Rect,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = remember(target) { Animatable(0f) }
    LaunchedEffect(target) {
        progress.animateTo(1f, tween(FLIGHT_MILLIS, easing = FastOutSlowInEasing))
        onFinished()
    }
    val fraction = progress.value
    val scale = lerp(1f, FLIGHT_END_SCALE, fraction)
    val alpha = if (fraction < FADE_START) 1f else lerp(1f, 0f, (fraction - FADE_START) / (1f - FADE_START))
    val center = lerp(target.from.center, target.to.center, fraction)
    val widthPx = target.from.width * scale
    val heightPx = target.from.height * scale
    val density = LocalDensity.current
    val topLeft = center - rootInWindow.topLeft
    ProductImageView(
        image = target.image,
        contentDescription = null,
        modifier = modifier
            .zIndex(1f)
            .offset {
                IntOffset(
                    (topLeft.x - widthPx / 2f).roundToInt(),
                    (topLeft.y - heightPx / 2f).roundToInt(),
                )
            }
            .size(
                width = with(density) { widthPx.toDp() },
                height = with(density) { heightPx.toDp() },
            )
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(28.dp * scale))
            .clearAndSetSemantics { },
    )
}

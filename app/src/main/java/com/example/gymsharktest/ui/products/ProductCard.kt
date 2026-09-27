package com.example.gymsharktest.ui.products

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gymsharktest.R
import com.example.gymsharktest.model.Product
import com.example.gymsharktest.ui.components.Badge
import com.example.gymsharktest.ui.components.LabelBadge
import com.example.gymsharktest.ui.components.PriceRow
import com.example.gymsharktest.ui.components.ProductImageView
import com.example.gymsharktest.ui.components.ShimmerBox
import com.example.gymsharktest.ui.components.displayText
import com.example.gymsharktest.ui.components.rememberPriceFormatter

/** Portrait crop matching the catalogue's own photography. */
private const val MEDIA_ASPECT_RATIO = 0.8f
private val MediaShape = RoundedCornerShape(22.dp)
private const val SOLD_OUT_IMAGE_ALPHA = 0.55f

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    favourite: Boolean = false,
    onFavouriteClick: () -> Unit = {},
) {
    val announcement = product.announceableSummary()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "card-press")

    Column(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MEDIA_ASPECT_RATIO),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MediaShape)
                    .clickable(interactionSource = interaction, indication = null, onClick = onClick)
                    .clearAndSetSemantics {
                        contentDescription = announcement
                        role = Role.Button
                        onClick {
                            onClick()
                            true
                        }
                    },
            ) {
            ProductImageView(
                image = product.featuredImage,
                contentDescription = null,
                fallbackLabel = product.title,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (product.inStock || !product.hasImage) 1f else SOLD_OUT_IMAGE_ALPHA),
            )

            val discount = product.cardDiscountPercent
            val cardLabel = product.cardLabel
            when {
                !product.inStock -> Badge(
                    text = stringResource(R.string.badge_sold_out),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                )
                discount != null -> Badge(
                    text = stringResource(R.string.badge_discount, discount),
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                )
                cardLabel != null -> LabelBadge(
                    label = cardLabel,
                    filled = true,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                )
            }
            }
            CardFavourite(
                favourite = favourite,
                onClick = onFavouriteClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
            )
        }

        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .clickable(interactionSource = interaction, indication = null, onClick = onClick)
                .clearAndSetSemantics { }
                .padding(horizontal = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (!product.colour.isNullOrBlank()) {
                Text(
                    text = product.colour,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (product.inStock) {
                PriceRow(
                    price = product.price,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp),
                )
                if (product.isLowStock) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.sizes_left,
                            product.remainingSizeCount,
                            product.remainingSizeCount,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.badge_sold_out),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun CardFavourite(
    favourite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (favourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = stringResource(
                if (favourite) R.string.action_remove_favourite else R.string.action_add_favourite,
            ),
            tint = if (favourite) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun Product.announceableSummary(): String {
    val formatter = rememberPriceFormatter()
    return listOfNotNull(
        title,
        colour?.takeIf(String::isNotBlank),
        formatter.format(price),
        if (!inStock) stringResource(R.string.badge_sold_out) else null,
        cardDiscountPercent?.let { stringResource(R.string.badge_discount, it) },
        cardLabel?.displayText(),
        if (isLowStock) {
            pluralStringResource(R.plurals.sizes_left, remainingSizeCount, remainingSizeCount)
        } else {
            null
        },
    ).joinToString(separator = ", ")
}

/** Placeholder cell used while the first load is in flight, sized like a real card so the grid does not jump. */
@Composable
fun ProductCardSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MEDIA_ASPECT_RATIO)
                .clip(MediaShape),
        )
        Spacer(Modifier.height(10.dp))
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp)),
        )
        Spacer(Modifier.height(6.dp))
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp)),
        )
    }
}

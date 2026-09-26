package com.example.gymsharktest.ui.products

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import com.example.gymsharktest.R
import com.example.gymsharktest.model.Product
import com.example.gymsharktest.model.ProductLabel
import com.example.gymsharktest.ui.components.Badge
import com.example.gymsharktest.ui.components.LabelBadge
import com.example.gymsharktest.ui.components.ProductImageView
import com.example.gymsharktest.ui.components.displayText
import com.example.gymsharktest.ui.components.rememberPriceFormatter

private val HeroShape = RoundedCornerShape(20.dp)

/** 1 when the card is snapped to the start of the carousel, fading as it scrolls away. */
private fun LazyListState.focusOf(index: Int): Float {
    val info = layoutInfo
    val item = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return 0.85f
    val distance = abs(item.offset - info.viewportStartOffset).toFloat()
    val size = item.size.toFloat().coerceAtLeast(1f)
    return 1f - (distance / size).coerceIn(0f, 1f)
}

/**
 * Draws into the grid's horizontal padding. The reported width stays the cell width so the grid
 * does not reflow, while the background is measured wide enough to reach both screen edges.
 */
private fun Modifier.bleedHorizontal(inset: Dp): Modifier = layout { measurable, constraints ->
    val extra = inset.roundToPx()
    val placeable = measurable.measure(
        constraints.copy(maxWidth = constraints.maxWidth + extra * 2),
    )
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-extra, 0)
    }
}
private const val HERO_ASPECT_RATIO = 1.15f
private const val HERO_WIDTH_FRACTION = 0.86f

/**
 * A merchandising row above the catalogue. Chips change this carousel only.
 * Renders nothing when no in-stock product carries a merchandising label.
 */
@Composable
fun FeaturedShelf(
    products: List<Product>,
    onProductClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    screenInset: Dp = 20.dp,
) {
    var selectedKey by rememberSaveable { mutableStateOf<String?>(null) }
    val labels = featuredLabels(products)
    val selected = labels.firstOrNull { it.shelfKey() == selectedKey }
    val featured = featuredProducts(products, selected)
    val rowState = rememberLazyListState()

    LaunchedEffect(selectedKey) {
        rowState.animateScrollToItem(0)
    }

    Column(
        modifier = modifier
            .bleedHorizontal(screenInset)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = screenInset, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(R.string.featured_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        LazyRow(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "all") {
                ShelfChip(
                    text = stringResource(R.string.featured_all),
                    selected = selected == null,
                    onClick = { selectedKey = null },
                )
            }
            items(labels, key = { it.shelfKey() }) { label ->
                ShelfChip(
                    text = label.displayText(),
                    selected = label == selected,
                    onClick = { selectedKey = label.shelfKey() },
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cardWidth = maxWidth * HERO_WIDTH_FRACTION
            LazyRow(
                state = rowState,
                flingBehavior = rememberSnapFlingBehavior(rowState),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = maxWidth - cardWidth),
            ) {
                itemsIndexed(featured, key = { _, product -> product.id }) { index, product ->
                    val focus = rowState.focusOf(index)
                    FeaturedHeroCard(
                        product = product,
                        selectedLabel = selected,
                        onClick = { onProductClick(product.id) },
                        modifier = Modifier
                            .width(cardWidth)
                            .animateItem()
                            .graphicsLayer {
                                val scale = 0.94f + (0.06f * focus)
                                scaleX = scale
                                scaleY = scale
                                alpha = 0.78f + (0.22f * focus)
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedHeroCard(
    product: Product,
    selectedLabel: ProductLabel?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val price = rememberPriceFormatter().format(product.price)
    val badgeLabel = product.cardLabel?.takeUnless { it == selectedLabel }
    val badgeText = badgeLabel?.displayText()
    val discount = product.cardDiscountPercent
    val discountText = if (discount != null) {
        stringResource(R.string.badge_discount, discount)
    } else {
        null
    }
    val announcement = listOfNotNull(product.title, price, badgeText, discountText)
        .joinToString(separator = ", ")

    Box(
        modifier = modifier
            .aspectRatio(HERO_ASPECT_RATIO)
            .clip(HeroShape)
            .clickable(onClick = onClick)
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
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.46f)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.62f)),
                    ),
                ),
        )
        when {
            discount != null && discountText != null -> Badge(
                text = discountText,
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
            )
            badgeLabel != null -> LabelBadge(
                label = badgeLabel,
                filled = true,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
        ) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = price,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun ShelfChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(180),
        label = "chip-container",
    )
    val content by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(180),
        label = "chip-content",
    )
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = content,
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(container)
                } else {
                    Modifier
                        .background(container)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                },
            )
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

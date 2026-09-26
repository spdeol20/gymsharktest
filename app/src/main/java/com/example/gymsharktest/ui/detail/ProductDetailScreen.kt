package com.example.gymsharktest.ui.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.content.res.Configuration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymsharktest.R
import com.example.gymsharktest.model.Product
import com.example.gymsharktest.model.ProductLabel
import com.example.gymsharktest.model.SizeAvailability
import com.example.gymsharktest.ui.components.LabelBadgeRow
import com.example.gymsharktest.ui.components.MessageState
import com.example.gymsharktest.ui.components.PriceRow
import com.example.gymsharktest.ui.components.rememberPriceFormatter
import com.example.gymsharktest.ui.components.ProductImageView
import com.example.gymsharktest.ui.components.ShimmerBox
import com.example.gymsharktest.ui.components.displayText
import com.example.gymsharktest.ui.text.rememberHtmlDescription

private const val MEDIA_ASPECT_RATIO = 0.92f
private const val WASH_ALPHA = 0.42f
private val TabletMinWidth = 600.dp
private const val DETAIL_IMAGE_WIDTH_PX = 1080
private val ImageShape = RoundedCornerShape(28.dp)
private val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
private val LandscapeSheetShape = RoundedCornerShape(28.dp)
private const val MAX_PAGER_DOTS = 6
private const val MIN_BASKET_QUANTITY = 1
private const val MAX_BASKET_QUANTITY = 10

@Composable
fun ProductDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProductDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    uiState: ProductDetailUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val showBasket = uiState is ProductDetailUiState.Content
            val basketVisible = remember { MutableTransitionState(false) }
            LaunchedEffect(showBasket) { basketVisible.targetState = showBasket }
            AnimatedVisibility(
                visibleState = basketVisible,
                enter = slideInVertically(
                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                ) { fullHeight -> fullHeight },
            ) {
                val product = (uiState as? ProductDetailUiState.Content)?.product
                if (product != null) {
                    BasketBar(product = product)
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val title = (uiState as? ProductDetailUiState.Content)?.product?.title
                        ?: stringResource(R.string.detail_screen_title)
                    AnimatedContent(
                        targetState = title,
                        transitionSpec = {
                            fadeIn(tween(220)) togetherWith fadeOut(tween(120))
                        },
                        label = "detail-title",
                    ) { value ->
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = { BackButton(onClick = onBackClick) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (uiState) {
                ProductDetailUiState.Loading -> DetailSkeleton()

                ProductDetailUiState.NotFound -> MessageState(
                    title = stringResource(R.string.empty_title),
                    body = stringResource(R.string.detail_not_found),
                )

                is ProductDetailUiState.Content -> DetailContent(product = uiState.product)
            }
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(start = 12.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.action_back),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DetailContent(product: Product, modifier: Modifier = Modifier) {
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val wash = rememberImageWash(product.featuredImage?.url)
    val washColor by animateColorAsState(
        targetValue = wash?.toComposeColor()?.copy(alpha = WASH_ALPHA) ?: Color.Transparent,
        animationSpec = tween(280),
        label = "image-wash",
    )
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableWidth = maxWidth
        val sideBySide = landscape || availableWidth >= TabletMinWidth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (washColor.alpha == 0f) {
                        Modifier
                    } else {
                        Modifier.background(imageWashBrush(washColor, sideBySide))
                    },
                ),
        ) {
            if (sideBySide) {
                SideBySideDetail(product = product, imageMaxWidth = availableWidth * 0.5f)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    MediaCarousel(
                        product = product,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    ProductSheet(
                        product = product,
                        modifier = Modifier.padding(top = 18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SideBySideDetail(
    product: Product,
    imageMaxWidth: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MediaCarousel(
            product = product,
            fitHeight = true,
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = imageMaxWidth)
                .aspectRatio(MEDIA_ASPECT_RATIO, matchHeightConstraintsFirst = true),
        )
        ProductSheet(
            product = product,
            shape = LandscapeSheetShape,
            scroll = true,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun ProductSheet(
    product: Product,
    modifier: Modifier = Modifier,
    shape: Shape = SheetShape,
    scroll: Boolean = false,
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (product.inStock && product.merchandisingLabels.isNotEmpty()) {
            LabelBadgeRow(labels = product.merchandisingLabels)
        }

        Text(
            text = product.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        val subtitle = listOfNotNull(
            product.colour?.takeIf(String::isNotBlank),
            product.type?.takeIf(String::isNotBlank),
        ).joinToString(separator = " \u00B7 ")
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        PriceRow(
            price = product.price,
            style = MaterialTheme.typography.titleLarge,
        )

        if (product.sizes.isNotEmpty()) {
            SectionTitle(stringResource(R.string.detail_sizes))
            SizeRow(sizes = product.sizes)
        }

        if (product.materialLabels.isNotEmpty()) {
            SectionTitle(stringResource(R.string.detail_materials))
            MaterialChipRow(labels = product.materialLabels)
        }

        DetailInfoRows(product = product)

        ProductActions(product = product)
    }
}

@Composable
private fun ProductActions(product: Product, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val price = rememberPriceFormatter().format(product.price)
    var askToReport by rememberSaveable(product.id) { mutableStateOf(false) }
    var reported by rememberSaveable(product.id) { mutableStateOf(false) }

    HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = modifier)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(
            onClick = {
                val message = context.getString(R.string.share_product, product.title, price)
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                context.startActivity(
                    Intent.createChooser(send, context.getString(R.string.action_share)),
                )
            },
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.action_share))
        }
        TextButton(
            onClick = { askToReport = true },
            enabled = !reported,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(
                    if (reported) R.string.action_reported else R.string.action_report,
                ),
            )
        }
    }

    if (askToReport) {
        AlertDialog(
            onDismissRequest = { askToReport = false },
            title = { Text(stringResource(R.string.report_title)) },
            text = { Text(stringResource(R.string.report_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        reported = true
                        askToReport = false
                    },
                ) {
                    Text(stringResource(R.string.action_report))
                }
            },
            dismissButton = {
                TextButton(onClick = { askToReport = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

private enum class DetailSheet { Description, Delivery }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailInfoRows(product: Product, modifier: Modifier = Modifier) {
    var openName by rememberSaveable(product.id) { mutableStateOf<String?>(null) }
    val open = openName?.let { runCatching { DetailSheet.valueOf(it) }.getOrNull() }

    Column(modifier) {
        InfoRow(
            label = stringResource(R.string.detail_description),
            onClick = { openName = DetailSheet.Description.name },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        InfoRow(
            label = stringResource(R.string.detail_delivery),
            onClick = { openName = DetailSheet.Delivery.name },
        )
    }

    val sheet = open
    if (sheet != null) {
        ModalBottomSheet(
            onDismissRequest = { openName = null },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(
                        if (sheet == DetailSheet.Description) {
                            R.string.detail_description
                        } else {
                            R.string.detail_delivery
                        },
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (sheet == DetailSheet.Description && product.descriptionHtml.isNotBlank()) {
                    Text(
                        text = rememberHtmlDescription(product.descriptionHtml),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = stringResource(
                            if (sheet == DetailSheet.Description) {
                                R.string.detail_description_empty
                            } else {
                                R.string.detail_delivery_body
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasketBar(product: Product, modifier: Modifier = Modifier) {
    var favourite by rememberSaveable(product.id) { mutableStateOf(false) }
    var added by rememberSaveable(product.id) { mutableStateOf(false) }
    var quantity by rememberSaveable(product.id) { mutableStateOf(MIN_BASKET_QUANTITY) }
    var sheetOpen by rememberSaveable(product.id) { mutableStateOf(false) }
    var selectedSize by rememberSaveable(product.id) { mutableStateOf<String?>(null) }
    val canBuy = product.inStock && !added

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            if (product.inStock && product.isLowStock) {
                Text(
                    text = pluralStringResource(
                        R.plurals.sizes_left,
                        product.remainingSizeCount,
                        product.remainingSizeCount,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FavouriteButton(
                    favourite = favourite,
                    onClick = { favourite = !favourite },
                )
                QuantityStepper(
                    quantity = quantity,
                    enabled = canBuy,
                    onDecrease = { quantity = (quantity - 1).coerceAtLeast(MIN_BASKET_QUANTITY) },
                    onIncrease = { quantity = (quantity + 1).coerceAtMost(MAX_BASKET_QUANTITY) },
                )
                BasketButton(
                    label = when {
                        !product.inStock -> stringResource(R.string.badge_sold_out)
                        added -> stringResource(R.string.action_added_to_basket)
                        else -> stringResource(R.string.action_add_to_basket)
                    },
                    enabled = canBuy,
                    emphasized = product.inStock,
                    onClick = {
                        if (product.sizes.isEmpty()) {
                            added = true
                        } else {
                            selectedSize = null
                            sheetOpen = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (sheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { sheetOpen = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            SizePickerSheet(
                product = product,
                quantity = quantity,
                selectedSize = selectedSize,
                onSizeSelected = { selectedSize = it },
                onAdd = {
                    added = true
                    sheetOpen = false
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SizePickerSheet(
    product: Product,
    quantity: Int,
    selectedSize: String?,
    onSizeSelected: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = product.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.detail_sizes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            product.sizes.forEach { size ->
                SelectableSizeChip(
                    size = size,
                    selected = size.size == selectedSize,
                    onClick = { onSizeSelected(size.size) },
                )
            }
        }
        Text(
            text = stringResource(R.string.sheet_quantity, quantity),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BasketButton(
            label = stringResource(R.string.action_add_to_basket),
            enabled = selectedSize != null,
            emphasized = selectedSize != null,
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SelectableSizeChip(
    size: SizeAvailability,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inStock = size.inStock
    val description = stringResource(R.string.action_select_size, size.size)
    Text(
        text = size.size,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = when {
            selected -> MaterialTheme.colorScheme.onPrimary
            inStock -> MaterialTheme.colorScheme.onSurface
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        textDecoration = if (inStock) null else TextDecoration.LineThrough,
        modifier = modifier
            .clip(CircleShape)
            .then(
                when {
                    selected -> Modifier.background(MaterialTheme.colorScheme.primary)
                    inStock -> Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    else -> Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                },
            )
            .clickable(enabled = inStock, onClick = onClick)
            .semantics {
                role = Role.RadioButton
                this.selected = selected
                contentDescription = description
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
private fun FavouriteButton(
    favourite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .background(MaterialTheme.colorScheme.surface)
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
        )
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    enabled: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuantityStep(
            symbol = "\u2212",
            contentDescription = stringResource(R.string.action_decrease_quantity),
            enabled = enabled && quantity > MIN_BASKET_QUANTITY,
            tint = content,
            onClick = onDecrease,
        )
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = content,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(24.dp),
        )
        QuantityStep(
            symbol = "+",
            contentDescription = stringResource(R.string.action_increase_quantity),
            enabled = enabled && quantity < MAX_BASKET_QUANTITY,
            tint = content,
            onClick = onIncrease,
        )
    }
}

@Composable
private fun QuantityStep(
    symbol: String,
    contentDescription: String,
    enabled: Boolean,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) tint else MaterialTheme.colorScheme.outline,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

@Composable
private fun BasketButton(
    label: String,
    enabled: Boolean,
    emphasized: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (emphasized) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val content = if (emphasized) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .height(52.dp)
            .clip(CircleShape)
            .background(container)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MediaCarousel(
    product: Product,
    modifier: Modifier = Modifier,
    fitHeight: Boolean = false,
) {
    val images = product.images
    val frame = if (fitHeight) {
        modifier.clip(ImageShape)
    } else {
        modifier
            .fillMaxWidth()
            .aspectRatio(MEDIA_ASPECT_RATIO)
            .clip(ImageShape)
    }

    if (images.isEmpty()) {
        ProductImageView(
            image = null,
            contentDescription = stringResource(R.string.product_image_of, product.title),
            fallbackLabel = product.title,
            modifier = frame,
        )
        return
    }

    val pagerState = rememberPagerState { images.size }

    Column(modifier = if (fitHeight) modifier else modifier.fillMaxWidth()) {
        val pagerModifier = if (fitHeight) {
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(ImageShape)
        } else {
            Modifier
                .fillMaxWidth()
                .aspectRatio(MEDIA_ASPECT_RATIO)
                .clip(ImageShape)
        }
        HorizontalPager(
            state = pagerState,
            modifier = pagerModifier,
        ) { page ->
            ProductImageView(
                image = images[page],
                contentDescription = stringResource(R.string.product_image_of, product.title),
                fallbackLabel = product.title,
                targetWidthPx = DETAIL_IMAGE_WIDTH_PX,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (images.size > 1) {
            PagerDots(
                pageCount = images.size,
                selectedPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 14.dp),
            )
        }
    }
}

@Composable
private fun PagerDots(
    pageCount: Int,
    selectedPage: Int,
    modifier: Modifier = Modifier,
) {
    if (pageCount > MAX_PAGER_DOTS) {
        Text(
            text = "${selectedPage + 1} / $pageCount",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { page ->
            val selected = page == selectedPage
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (selected) 18.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                    ),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MaterialChipRow(labels: List<ProductLabel>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEach { label ->
            Text(
                text = label.displayText(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SizeRow(sizes: List<SizeAvailability>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        sizes.forEach { size -> SizeChip(size) }
    }
}

@Composable
private fun SizeChip(size: SizeAvailability, modifier: Modifier = Modifier) {
    val inStock = size.inStock
    Text(
        text = size.size,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = if (inStock) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        textDecoration = if (inStock) null else TextDecoration.LineThrough,
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (inStock) {
                    Modifier.background(MaterialTheme.colorScheme.primary)
                } else {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                },
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(top = 4.dp),
    )
}

@Composable
private fun DetailSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MEDIA_ASPECT_RATIO)
                .clip(ImageShape),
        )
        Spacer(Modifier.height(18.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SheetShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.45f).height(28.dp).clip(CircleShape))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(22.dp).clip(CircleShape))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.35f).height(18.dp).clip(CircleShape))
        }
    }
}

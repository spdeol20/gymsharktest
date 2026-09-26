package com.example.gymsharktest.ui.detail

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymsharktest.R
import com.example.gymsharktest.model.Product
import com.example.gymsharktest.model.ProductLabel
import com.example.gymsharktest.model.SizeAvailability
import com.example.gymsharktest.ui.components.Badge
import com.example.gymsharktest.ui.components.LabelBadgeRow
import com.example.gymsharktest.ui.components.displayText
import com.example.gymsharktest.ui.components.MessageState
import com.example.gymsharktest.ui.components.PriceRow
import com.example.gymsharktest.ui.components.ProductImageView
import com.example.gymsharktest.ui.components.ShimmerBox
import com.example.gymsharktest.ui.text.rememberHtmlDescription

private const val MEDIA_ASPECT_RATIO = 0.8f
private const val DETAIL_IMAGE_WIDTH_PX = 1080

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
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val title = (uiState as? ProductDetailUiState.Content)?.product?.title
                    Text(
                        text = title.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    // A labelled text button rather than a vector icon: it reads correctly to a
                    // screen reader without a separate content description.
                    TextButton(onClick = onBackClick) {
                        Text(stringResource(R.string.action_back))
                    }
                },
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
private fun DetailContent(product: Product, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        MediaCarousel(product = product)

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (product.inStock && product.merchandisingLabels.isNotEmpty()) {
                LabelBadgeRow(labels = product.merchandisingLabels)
            }

            Text(
                text = product.title,
                style = MaterialTheme.typography.headlineMedium,
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PriceRow(
                    price = product.price,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (!product.inStock) {
                    Badge(
                        text = stringResource(R.string.badge_sold_out),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (product.isLowStock) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.sizes_left,
                            product.remainingSizeCount,
                            product.remainingSizeCount,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            if (product.sizes.isNotEmpty()) {
                SectionDivider()
                SectionTitle(stringResource(R.string.detail_sizes))
                SizeRow(sizes = product.sizes)
            }

            if (product.materialLabels.isNotEmpty()) {
                SectionDivider()
                SectionTitle(stringResource(R.string.detail_materials))
                MaterialChipRow(labels = product.materialLabels)
            }

            if (product.descriptionHtml.isNotBlank()) {
                SectionDivider()
                SectionTitle(stringResource(R.string.detail_description))
                // Sanitised and converted once per description, cached by the remember key.
                Text(
                    text = rememberHtmlDescription(product.descriptionHtml),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MediaCarousel(product: Product, modifier: Modifier = Modifier) {
    val images = product.images

    if (images.isEmpty()) {
        ProductImageView(
            image = null,
            contentDescription = null,
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(MEDIA_ASPECT_RATIO),
        )
        return
    }

    val pagerState = rememberPagerState { images.size }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MEDIA_ASPECT_RATIO),
        ) { page ->
            ProductImageView(
                image = images[page],
                contentDescription = stringResource(R.string.product_image_of, product.title),
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
                    .padding(top = 12.dp),
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
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(pageCount) { page ->
            val selected = page == selectedPage
            Box(
                modifier = Modifier
                    .size(if (selected) 8.dp else 6.dp)
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
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(8.dp),
                    )
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
    Text(
        text = size.size,
        style = MaterialTheme.typography.bodyMedium,
        color = if (size.inStock) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        // Struck through rather than hidden: knowing a size exists but is gone is useful.
        textDecoration = if (size.inStock) null else TextDecoration.LineThrough,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
    )
}

@Composable
private fun SectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
private fun DetailSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(MEDIA_ASPECT_RATIO),
        )
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.3f).height(14.dp))
        }
    }
}

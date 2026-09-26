package com.example.gymsharktest.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymsharktest.R
import com.example.gymsharktest.model.Price
import com.example.gymsharktest.model.Product
import com.example.gymsharktest.model.ProductLabel
import com.example.gymsharktest.model.SizeAvailability
import com.example.gymsharktest.ui.components.ErrorState
import com.example.gymsharktest.ui.components.MessageState
import com.example.gymsharktest.ui.components.StaleDataBanner
import com.example.gymsharktest.ui.theme.GymsharkTheme

/** Adaptive so a phone shows two columns and a tablet or landscape shows more without a second layout. */
private val MinCellWidth = 168.dp
private const val SKELETON_CELL_COUNT = 6

@Composable
fun ProductListScreen(
    onProductClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProductListScreen(
        uiState = uiState,
        onProductClick = onProductClick,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

/**
 * Stateless half of the screen: takes the state and emits events, so it can be driven from a
 * preview or a Compose test without Hilt or a network.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    uiState: ProductListUiState,
    onProductClick: (Long) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.catalogue_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (uiState.products.isNotEmpty()) {
                            Text(
                                text = stringResource(
                                    R.string.catalogue_subtitle,
                                    uiState.products.size,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.showFullScreenError -> ErrorState(
                    error = requireNotNull(uiState.error),
                    onRetry = onRetry,
                )

                uiState.showEmpty -> MessageState(
                    title = stringResource(R.string.empty_title),
                    body = stringResource(R.string.empty_body),
                    onRetry = onRetry,
                )

                else -> ProductGrid(
                    uiState = uiState,
                    onProductClick = onProductClick,
                )
            }
        }
    }
}

@Composable
private fun ProductGrid(
    uiState: ProductListUiState,
    onProductClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(MinCellWidth),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (uiState.showStaleWarning) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                StaleDataBanner()
            }
        }

        if (uiState.showSkeleton) {
            items(SKELETON_CELL_COUNT) { ProductCardSkeleton() }
        } else {
            items(
                items = uiState.products,
                // Stable keys keep scroll position and image state across a refresh.
                key = { product -> product.id },
            ) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product.id) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun ProductListPreview() {
    GymsharkTheme {
        ProductListScreen(
            uiState = ProductListUiState(products = previewProducts()),
            onProductClick = {},
            onRefresh = {},
            onRetry = {},
        )
    }
}

private fun previewProducts(): List<Product> = listOf(
    Product(
        id = 1,
        sku = "A-1",
        title = "Speed Leggings",
        colour = "Black",
        type = "Leggings",
        descriptionHtml = "",
        price = Price(amountMinorUnits = 1000, compareAtMinorUnits = 2000),
        inStock = true,
        labels = listOf(ProductLabel.Sale),
    ),
    Product(
        id = 2,
        sku = "A-2",
        title = "Apex Seamless T-Shirt",
        colour = "Willow Green",
        type = "T-Shirt",
        descriptionHtml = "",
        price = Price(amountMinorUnits = 3200),
        inStock = false,
        labels = listOf(ProductLabel.GoingFast),
    ),
    Product(
        id = 3,
        sku = "A-3",
        title = "Training Graphic T-Shirt",
        colour = "Black",
        type = "T-Shirt",
        descriptionHtml = "",
        price = Price(amountMinorUnits = 2500),
        inStock = true,
        labels = listOf(ProductLabel.New, ProductLabel.RecycledPolyester),
        sizes = listOf(
            SizeAvailability("S", inStock = true),
            SizeAvailability("M", inStock = true),
            SizeAvailability("L", inStock = false),
        ),
    ),
)

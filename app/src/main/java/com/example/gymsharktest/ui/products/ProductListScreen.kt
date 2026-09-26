package com.example.gymsharktest.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
private val GridHorizontalPadding = 20.dp
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
    var sort by rememberSaveable { mutableStateOf(CatalogueSort.Catalogue) }
    val gridProducts = remember(uiState.products, sort) { uiState.products.sortedFor(sort) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.catalogue_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (uiState.products.isNotEmpty()) {
                            Text(
                                text = stringResource(
                                    R.string.catalogue_subtitle,
                                    uiState.products.size,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.products.isNotEmpty()) {
                        SortMenu(
                            sort = sort,
                            onSortChange = { sort = it },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
                    gridProducts = gridProducts,
                    onProductClick = onProductClick,
                )
            }
        }
    }
}

@Composable
private fun ProductGrid(
    uiState: ProductListUiState,
    gridProducts: List<Product>,
    onProductClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(MinCellWidth),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = GridHorizontalPadding,
            end = GridHorizontalPadding,
            top = 8.dp,
            bottom = 28.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        if (uiState.showStaleWarning) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                StaleDataBanner()
            }
        }

        if (uiState.showSkeleton) {
            items(SKELETON_CELL_COUNT) { ProductCardSkeleton() }
        } else {
            if (featuredLabels(uiState.products).isNotEmpty()) {
                item(key = "featured-shelf", span = { GridItemSpan(maxLineSpan) }) {
                    FeaturedShelf(
                        products = uiState.products,
                        onProductClick = onProductClick,
                        screenInset = GridHorizontalPadding,
                    )
                }
                item(key = "all-products-heading", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = stringResource(
                            R.string.catalogue_all_products,
                            uiState.products.size,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 20.dp),
                    )
                }
            }
            items(
                items = gridProducts,
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

@Composable
private fun SortMenu(
    sort: CatalogueSort,
    onSortChange: (CatalogueSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.Sort,
                contentDescription = stringResource(R.string.sort_products),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            CatalogueSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    onClick = {
                        onSortChange(option)
                        expanded = false
                    },
                    trailingIcon = if (option == sort) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

private val CatalogueSort.labelRes: Int
    get() = when (this) {
        CatalogueSort.Catalogue -> R.string.sort_catalogue
        CatalogueSort.PriceLowToHigh -> R.string.sort_price_low
        CatalogueSort.PriceHighToLow -> R.string.sort_price_high
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
    Product(
        id = 4,
        sku = "A-4",
        title = "Crest Hoodie",
        colour = "Charcoal",
        type = "Hoodie",
        descriptionHtml = "",
        price = Price(amountMinorUnits = 5500),
        inStock = true,
        labels = listOf(ProductLabel.LimitedEdition),
    ),
)

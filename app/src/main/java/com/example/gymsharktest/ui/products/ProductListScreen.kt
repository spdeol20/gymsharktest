package com.example.gymsharktest.ui.products

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.gymsharktest.BuildConfig
import com.example.gymsharktest.R
import com.example.gymsharktest.model.CatalogueSort
import com.example.gymsharktest.model.Price
import com.example.gymsharktest.model.Product
import com.example.gymsharktest.model.ProductLabel
import com.example.gymsharktest.model.SizeAvailability
import com.example.gymsharktest.model.sortedFor
import com.example.gymsharktest.ui.components.displayText
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
    val pagingItems = viewModel.products.collectAsLazyPagingItems()

    if (BuildConfig.DEBUG) {
        LaunchedEffect(pagingItems.itemCount, pagingItems.loadState) {
            Log.d(
                "CataloguePaging",
                "items=${pagingItems.itemCount} append=${pagingItems.loadState.append}",
            )
        }
    }

    ProductListScreen(
        uiState = uiState,
        onProductClick = onProductClick,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        onSortChange = viewModel::onSortChange,
        onLabelChange = viewModel::onLabelChange,
        pagingItems = pagingItems,
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
    onSortChange: (CatalogueSort) -> Unit,
    onLabelChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    pagingItems: LazyPagingItems<Product>? = null,
    gridProducts: List<Product> = emptyList(),
) {
    val favouriteIds = rememberSaveable(
        saver = listSaver<SnapshotStateList<Long>, Long>(
            save = { ids -> ids.toList() },
            restore = { saved -> mutableStateListOf<Long>().apply { addAll(saved) } },
        ),
    ) { mutableStateListOf<Long>() }
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
                        if (uiState.productCount > 0) {
                            Text(
                                text = stringResource(
                                    R.string.catalogue_subtitle,
                                    if (uiState.labelKey == null) uiState.productCount else uiState.gridCount,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.productCount > 0) {
                        SortMenu(
                            sort = uiState.sort,
                            onSortChange = onSortChange,
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
                    pagingItems = pagingItems,
                    gridProducts = gridProducts,
                    onProductClick = onProductClick,
                    onLabelChange = onLabelChange,
                    favouriteIds = favouriteIds,
                    onFavouriteClick = { productId ->
                        if (productId in favouriteIds) {
                            favouriteIds.remove(productId)
                        } else {
                            favouriteIds.add(productId)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ProductGrid(
    uiState: ProductListUiState,
    pagingItems: LazyPagingItems<Product>?,
    gridProducts: List<Product>,
    onProductClick: (Long) -> Unit,
    onLabelChange: (String?) -> Unit,
    favouriteIds: SnapshotStateList<Long>,
    onFavouriteClick: (Long) -> Unit,
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
                StaleDataBanner(error = requireNotNull(uiState.error))
            }
        }

        if (uiState.showSkeleton) {
            items(SKELETON_CELL_COUNT) { ProductCardSkeleton() }
        } else {
            val featured = featuredLabels(uiState.featured)
            if (featured.isNotEmpty()) {
                item(key = "featured-shelf", span = { GridItemSpan(maxLineSpan) }) {
                    FeaturedShelf(
                        products = uiState.featured,
                        selectedKey = uiState.labelKey,
                        onLabelChange = onLabelChange,
                        onProductClick = onProductClick,
                        screenInset = GridHorizontalPadding,
                    )
                }
                item(key = "all-products-heading", span = { GridItemSpan(maxLineSpan) }) {
                    val selected = featured.firstOrNull { it.shelfKey() == uiState.labelKey }
                    val shownCount = if (selected == null) uiState.productCount else uiState.gridCount
                    Text(
                        text = if (selected == null) {
                            stringResource(R.string.catalogue_all_products, shownCount)
                        } else {
                            stringResource(
                                R.string.catalogue_filtered_products,
                                selected.displayText(),
                                shownCount,
                            )
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 20.dp),
                    )
                }
            }
            if (pagingItems != null) {
                pagingProductItems(pagingItems, onProductClick, favouriteIds, onFavouriteClick)
            } else {
                items(
                    items = gridProducts,
                    key = { product -> product.id },
                ) { product ->
                    ProductCard(
                        product = product,
                        favourite = product.id in favouriteIds,
                        onFavouriteClick = { onFavouriteClick(product.id) },
                        onClick = { onProductClick(product.id) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

private fun LazyGridScope.pagingProductItems(
    pagingItems: LazyPagingItems<Product>,
    onProductClick: (Long) -> Unit,
    favouriteIds: SnapshotStateList<Long>,
    onFavouriteClick: (Long) -> Unit,
) {
    if (pagingItems.itemCount == 0) {
        items(SKELETON_CELL_COUNT) { ProductCardSkeleton() }
        return
    }
    items(
        count = pagingItems.itemCount,
        key = pagingItems.itemKey { product -> product.id },
    ) { index ->
        val product = pagingItems[index]
        if (product == null) {
            ProductCardSkeleton()
        } else {
            ProductCard(
                product = product,
                favourite = product.id in favouriteIds,
                onFavouriteClick = { onFavouriteClick(product.id) },
                onClick = { onProductClick(product.id) },
                modifier = Modifier.animateItem(),
            )
        }
    }
    when (val append = pagingItems.loadState.append) {
        is LoadState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is LoadState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
            TextButton(onClick = pagingItems::retry) {
                Text(stringResource(R.string.action_retry))
            }
        }
        is LoadState.NotLoading -> Unit
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
        var sort by remember { mutableStateOf(CatalogueSort.Catalogue) }
        var labelKey by remember { mutableStateOf<String?>(null) }
        val products = previewProducts()
        val selected = featuredLabels(products).firstOrNull { it.shelfKey() == labelKey }
        val grid = remember(products, sort, selected) {
            products
                .filter { selected == null || selected in it.labels }
                .sortedFor(sort)
        }
        ProductListScreen(
            uiState = ProductListUiState(
                featured = products.filter { it.inStock && it.merchandisingLabels.isNotEmpty() },
                productCount = products.size,
                gridCount = grid.size,
                labelKey = labelKey,
                sort = sort,
            ),
            gridProducts = grid,
            onProductClick = {},
            onRefresh = {},
            onRetry = {},
            onSortChange = { sort = it },
            onLabelChange = { labelKey = it },
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

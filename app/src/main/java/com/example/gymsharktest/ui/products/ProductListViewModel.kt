package com.example.gymsharktest.ui.products

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.gymsharktest.core.AppError
import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.data.repository.ProductRepository
import com.example.gymsharktest.model.CatalogueSort
import com.example.gymsharktest.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ProductListUiState(
    val featured: List<Product> = emptyList(),
    val productCount: Int = 0,
    val gridCount: Int = 0,
    val labelKey: String? = null,
    val sort: CatalogueSort = CatalogueSort.Catalogue,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
) {
    /** Nothing cached and the load failed: the only thing to show is the failure. */
    val showFullScreenError: Boolean get() = error != null && productCount == 0

    /** Cached products are on screen but the latest refresh failed, so say so without hiding them. */
    val showStaleWarning: Boolean get() = error != null && productCount > 0

    val showSkeleton: Boolean get() = isLoading && productCount == 0

    val showEmpty: Boolean get() = !isLoading && error == null && productCount == 0
}

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState(isLoading = true))
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    /**
     * Pages follow the saved sort. Refresh replaces the table and leaves this choice alone, so a
     * pull-to-refresh does not jump the shopper back to catalogue order.
     */
    val products: Flow<PagingData<Product>> = combine(
        savedStateHandle.getStateFlow(SORT_KEY, CatalogueSort.Catalogue.name),
        savedStateHandle.getStateFlow(LABEL_KEY, ""),
    ) { sort, label -> sort to label }
        .flatMapLatest { (sort, label) ->
            repository.pagedProducts(CatalogueSort.valueOf(sort), label.ifEmpty { null })
        }
        .cachedIn(viewModelScope)

    init {
        observeCache()
        load(isUserInitiated = false)
    }

    fun refresh() = load(isUserInitiated = true)

    fun retry() = load(isUserInitiated = false)

    fun onSortChange(sort: CatalogueSort) {
        savedStateHandle[SORT_KEY] = sort.name
    }

    fun onLabelChange(labelKey: String?) {
        savedStateHandle[LABEL_KEY] = labelKey.orEmpty()
    }

    private fun observeCache() {
        viewModelScope.launch {
            savedStateHandle.getStateFlow(SORT_KEY, CatalogueSort.Catalogue.name).collect { name ->
                _uiState.update { it.copy(sort = CatalogueSort.valueOf(name)) }
            }
        }
        viewModelScope.launch {
            savedStateHandle.getStateFlow(LABEL_KEY, "").collect { saved ->
                _uiState.update { it.copy(labelKey = saved.ifEmpty { null }) }
            }
        }
        viewModelScope.launch {
            repository.observeProductCount().collect { count ->
                _uiState.update { it.copy(productCount = count) }
            }
        }
        viewModelScope.launch {
            savedStateHandle.getStateFlow(LABEL_KEY, "")
                .flatMapLatest { saved -> repository.observeGridCount(saved.ifEmpty { null }) }
                .collect { count ->
                    _uiState.update { it.copy(gridCount = count) }
                }
        }
        viewModelScope.launch {
            repository.observeFeatured().collect { featured ->
                _uiState.update { it.copy(featured = featured) }
                val key = savedStateHandle.get<String>(LABEL_KEY).orEmpty()
                if (key.isNotEmpty() && featured.none { product -> product.labels.any { it.shelfKey() == key } }) {
                    savedStateHandle[LABEL_KEY] = ""
                }
            }
        }
    }

    private fun load(isUserInitiated: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isUserInitiated,
                    isRefreshing = isUserInitiated,
                    error = null,
                )
            }

            val result = repository.refresh()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = (result as? AppResult.Failure)?.error,
                )
            }
        }
    }

    private companion object {
        const val SORT_KEY = "catalogue_sort"
        const val LABEL_KEY = "catalogue_label"
    }
}

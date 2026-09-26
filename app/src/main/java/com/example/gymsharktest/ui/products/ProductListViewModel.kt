package com.example.gymsharktest.ui.products

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymsharktest.core.AppError
import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.data.repository.ProductRepository
import com.example.gymsharktest.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
) {
    /** Nothing cached and the load failed: the only thing to show is the failure. */
    val showFullScreenError: Boolean get() = error != null && products.isEmpty()

    /** Cached products are on screen but the latest refresh failed, so say so without hiding them. */
    val showStaleWarning: Boolean get() = error != null && products.isNotEmpty()

    val showSkeleton: Boolean get() = isLoading && products.isEmpty()

    val showEmpty: Boolean get() = !isLoading && error == null && products.isEmpty()
}

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val repository: ProductRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState(isLoading = true))
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init {
        observeCache()
        load(isUserInitiated = false)
    }

    fun refresh() = load(isUserInitiated = true)

    fun retry() = load(isUserInitiated = false)

    private fun observeCache() {
        viewModelScope.launch {
            repository.observeProducts().collect { products ->
                _uiState.update { it.copy(products = products) }
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
}

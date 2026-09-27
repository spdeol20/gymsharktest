package com.example.gymsharktest.ui.detail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.gymsharktest.data.repository.ProductRepository
import com.example.gymsharktest.model.Product
import com.example.gymsharktest.ui.navigation.ProductDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Immutable
sealed interface ProductDetailUiState {

    data object Loading : ProductDetailUiState

    @Immutable
    data class Content(val product: Product) : ProductDetailUiState

    /** An id that is not in the catalogue must render a message, never crash. */
    data object NotFound : ProductDetailUiState
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ProductRepository,
) : ViewModel() {

    private val productId = savedStateHandle.toRoute<ProductDetailRoute>().productId

    private val _uiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Loading)
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _cartCount = MutableStateFlow(0)
    val cartCount: StateFlow<Int> = _cartCount.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeCartCount().collect { _cartCount.value = it }
        }
        viewModelScope.launch {
            // Re-read by id rather than passing the product through the back stack, so the screen
            // still works from a deep link and survives process death. Room emits again if a
            // refresh replaces the row while the screen is open.
            repository.observeProduct(productId).collect { product ->
                _uiState.value = product
                    ?.let(ProductDetailUiState::Content)
                    ?: ProductDetailUiState.NotFound
            }
        }
    }

    fun addToCart(size: String?, quantity: Int) {
        viewModelScope.launch {
            repository.addToCart(productId, size, quantity)
        }
    }
}

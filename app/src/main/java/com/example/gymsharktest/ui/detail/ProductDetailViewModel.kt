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

    init {
        viewModelScope.launch {
            // Re-read by id rather than passing the product through the back stack, so the screen
            // still works from a deep link and survives process death.
            val product = repository.productById(productId)
            _uiState.value = product
                ?.let(ProductDetailUiState::Content)
                ?: ProductDetailUiState.NotFound
        }
    }
}

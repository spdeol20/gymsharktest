package com.example.gymsharktest.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymsharktest.data.repository.ProductRepository
import com.example.gymsharktest.model.CartLine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: ProductRepository,
) : ViewModel() {

    private val _lines = MutableStateFlow<List<CartLine>>(emptyList())
    val lines: StateFlow<List<CartLine>> = _lines.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeCart().collect { _lines.value = it }
        }
    }

    fun setQuantity(productId: Long, size: String?, quantity: Int) {
        viewModelScope.launch {
            repository.setCartQuantity(productId, size, quantity)
        }
    }
}

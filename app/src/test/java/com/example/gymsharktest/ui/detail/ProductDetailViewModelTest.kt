package com.example.gymsharktest.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.example.gymsharktest.util.FakeProductRepository
import com.example.gymsharktest.util.MainDispatcherRule
import com.example.gymsharktest.util.testProduct
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * The route is a `@Serializable` data class with a single `productId`, so navigation stores it
     * in the handle under that name. Building the handle directly keeps the test free of a
     * NavController.
     */
    private fun handleFor(productId: Long) = SavedStateHandle(mapOf("productId" to productId))

    @Test
    fun `an id in the catalogue resolves to content`() = runTest {
        val product = testProduct(id = 42, title = "Speed Leggings")
        val repository = FakeProductRepository(initialProducts = listOf(product))

        val state = ProductDetailViewModel(handleFor(42L), repository).uiState.value

        assertEquals(ProductDetailUiState.Content(product), state)
    }

    @Test
    fun `an id that is not in the catalogue renders not found rather than crashing`() = runTest {
        val repository = FakeProductRepository(initialProducts = listOf(testProduct(id = 1)))

        val state = ProductDetailViewModel(handleFor(999L), repository).uiState.value

        assertEquals(ProductDetailUiState.NotFound, state)
    }

    @Test
    fun `an empty catalogue renders not found`() = runTest {
        val repository = FakeProductRepository()

        val state = ProductDetailViewModel(handleFor(1L), repository).uiState.value

        assertTrue(state is ProductDetailUiState.NotFound)
    }
}

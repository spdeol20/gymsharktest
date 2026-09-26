package com.example.gymsharktest.ui.detail

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.testing.invoke
import com.example.gymsharktest.ui.navigation.ProductDetailRoute
import com.example.gymsharktest.util.FakeProductRepository
import com.example.gymsharktest.util.MainDispatcherRule
import com.example.gymsharktest.util.testProduct
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], application = Application::class)
class ProductDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * Navigation writes the route through a Bundle. A hand-built map never reaches that Bundle, so
     * [androidx.navigation.toRoute] crashes in a plain unit test. This helper writes the route the
     * same way navigation does, and Robolectric supplies the Bundle.
     */
    private fun handleFor(productId: Long) = SavedStateHandle(ProductDetailRoute(productId))

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

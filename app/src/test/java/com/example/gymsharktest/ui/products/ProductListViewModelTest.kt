package com.example.gymsharktest.ui.products

import com.example.gymsharktest.core.AppError
import com.example.gymsharktest.util.FakeProductRepository
import com.example.gymsharktest.util.MainDispatcherRule
import com.example.gymsharktest.util.testProduct
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProductRepository()

    @Test
    fun `products fetched on start are exposed to the UI`() = runTest {
        val products = listOf(testProduct(id = 1), testProduct(id = 2))
        repository.succeedWith(products)

        val state = ProductListViewModel(repository).uiState.value

        assertEquals(products, state.products)
        assertFalse(state.isLoading)
        assertEquals(null, state.error)
    }

    @Test
    fun `the catalogue is fetched exactly once on start`() = runTest {
        repository.succeedWith(listOf(testProduct()))

        ProductListViewModel(repository)

        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun `a failure with nothing cached asks for the full screen error`() = runTest {
        repository.failWith(AppError.Network)

        val state = ProductListViewModel(repository).uiState.value

        assertEquals(AppError.Network, state.error)
        assertTrue(state.showFullScreenError)
        assertFalse(state.showStaleWarning)
    }

    @Test
    fun `a failure with a warm cache keeps the products and warns instead`() = runTest {
        val cached = listOf(testProduct(id = 1))
        repository.succeedWith(cached)
        val viewModel = ProductListViewModel(repository)

        repository.failWith(AppError.Timeout)
        viewModel.refresh()

        val state = viewModel.uiState.value
        assertEquals(cached, state.products)
        assertTrue(state.showStaleWarning)
        assertFalse(state.showFullScreenError)
    }

    @Test
    fun `an empty successful response asks for the empty state, not an error`() = runTest {
        repository.succeedWith(emptyList())

        val state = ProductListViewModel(repository).uiState.value

        assertTrue(state.showEmpty)
        assertFalse(state.showFullScreenError)
        assertEquals(null, state.error)
    }

    @Test
    fun `retry clears the previous error once it succeeds`() = runTest {
        repository.failWith(AppError.Network)
        val viewModel = ProductListViewModel(repository)
        assertEquals(AppError.Network, viewModel.uiState.value.error)

        repository.succeedWith(listOf(testProduct(id = 3)))
        viewModel.retry()

        val state = viewModel.uiState.value
        assertEquals(null, state.error)
        assertEquals(1, state.products.size)
    }

    @Test
    fun `refresh settles with both progress flags cleared`() = runTest {
        repository.succeedWith(listOf(testProduct()))
        val viewModel = ProductListViewModel(repository)

        viewModel.refresh()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertEquals(2, repository.refreshCount)
    }

    @Test
    fun `the skeleton shows only while the first load has nothing to display`() {
        val loading = ProductListUiState(isLoading = true)
        val loadingWithCache = ProductListUiState(
            products = listOf(testProduct()),
            isLoading = true,
        )

        assertTrue(loading.showSkeleton)
        assertFalse(loadingWithCache.showSkeleton)
    }
}

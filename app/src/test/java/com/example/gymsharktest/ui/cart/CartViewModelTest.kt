package com.example.gymsharktest.ui.cart

import com.example.gymsharktest.model.SizeAvailability
import com.example.gymsharktest.util.FakeProductRepository
import com.example.gymsharktest.util.MainDispatcherRule
import com.example.gymsharktest.util.testProduct
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `adding a product and size emits that line`() = runTest {
        val product = testProduct(id = 7)
        val repository = FakeProductRepository(initialProducts = listOf(product))
        val viewModel = CartViewModel(repository)

        assertTrue(repository.addToCart(productId = 7, size = "M", quantity = 1))

        val line = viewModel.lines.value.single()
        assertEquals(7L, line.product.id)
        assertEquals("M", line.size)
        assertEquals(1, line.quantity)
    }

    @Test
    fun `adding the same line again increases quantity up to ten`() = runTest {
        val product = testProduct(id = 7)
        val repository = FakeProductRepository(initialProducts = listOf(product))
        val viewModel = CartViewModel(repository)

        assertTrue(repository.addToCart(productId = 7, size = "M", quantity = 6))
        assertTrue(repository.addToCart(productId = 7, size = "M", quantity = 6))

        assertEquals(10, viewModel.lines.value.single().quantity)
    }

    @Test
    fun `a sold out product is rejected`() = runTest {
        val product = testProduct(id = 7, inStock = false)
        val repository = FakeProductRepository(initialProducts = listOf(product))
        val viewModel = CartViewModel(repository)

        assertFalse(repository.addToCart(productId = 7, size = "M", quantity = 1))
        assertTrue(viewModel.lines.value.isEmpty())
    }

    @Test
    fun `an unknown size is rejected`() = runTest {
        val product = testProduct(
            id = 7,
            sizes = listOf(SizeAvailability(size = "M", inStock = true)),
        )
        val repository = FakeProductRepository(initialProducts = listOf(product))
        val viewModel = CartViewModel(repository)

        assertFalse(repository.addToCart(productId = 7, size = "XL", quantity = 1))
        assertTrue(viewModel.lines.value.isEmpty())
    }
}

package com.example.gymsharktest.data.repository

import com.example.gymsharktest.core.AppError
import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.data.remote.ProductRemoteDataSource
import com.example.gymsharktest.util.testProduct
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductRepositoryImplTest {

    private val remote: ProductRemoteDataSource = mockk()
    private val repository = ProductRepositoryImpl(remote)

    @Test
    fun `cache starts empty`() = runTest {
        assertEquals(emptyList<Nothing>(), repository.observeProducts().first())
    }

    @Test
    fun `a successful refresh publishes the fetched products`() = runTest {
        val products = listOf(testProduct(id = 1), testProduct(id = 2))
        coEvery { remote.fetchProducts() } returns AppResult.Success(products)

        val result = repository.refresh()

        assertTrue(result is AppResult.Success)
        assertEquals(products, repository.observeProducts().first())
    }

    @Test
    fun `a refresh replaces the previous contents rather than appending`() = runTest {
        coEvery { remote.fetchProducts() } returns AppResult.Success(listOf(testProduct(id = 1)))
        repository.refresh()

        coEvery { remote.fetchProducts() } returns AppResult.Success(listOf(testProduct(id = 9)))
        repository.refresh()

        val cached = repository.observeProducts().first()
        assertEquals(1, cached.size)
        assertEquals(9L, cached.single().id)
    }

    @Test
    fun `a failed refresh keeps the warm cache intact`() = runTest {
        val products = listOf(testProduct(id = 1))
        coEvery { remote.fetchProducts() } returns AppResult.Success(products)
        repository.refresh()

        coEvery { remote.fetchProducts() } returns AppResult.Failure(AppError.Network)
        val result = repository.refresh()

        assertEquals(AppResult.Failure(AppError.Network), result)
        assertEquals(products, repository.observeProducts().first())
    }

    @Test
    fun `a failed refresh propagates the error unchanged`() = runTest {
        coEvery { remote.fetchProducts() } returns AppResult.Failure(AppError.Timeout)

        val result = repository.refresh()

        assertEquals(AppError.Timeout, (result as AppResult.Failure).error)
    }

    @Test
    fun `productById returns the cached product`() = runTest {
        coEvery { remote.fetchProducts() } returns
            AppResult.Success(listOf(testProduct(id = 7, title = "Speed Leggings")))
        repository.refresh()

        assertEquals("Speed Leggings", repository.productById(7L)?.title)
    }

    @Test
    fun `productById returns null for an id that is not cached`() = runTest {
        coEvery { remote.fetchProducts() } returns AppResult.Success(listOf(testProduct(id = 7)))
        repository.refresh()

        assertEquals(null, repository.productById(8L))
    }
}

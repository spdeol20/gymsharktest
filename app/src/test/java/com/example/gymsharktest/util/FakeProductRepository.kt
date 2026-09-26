package com.example.gymsharktest.util

import androidx.paging.PagingData
import com.example.gymsharktest.core.AppError
import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.data.repository.ProductRepository
import com.example.gymsharktest.model.CatalogueSort
import com.example.gymsharktest.model.Product
import com.example.gymsharktest.model.sortedFor
import com.example.gymsharktest.ui.products.featuredProducts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Hand-written rather than mocked: the ViewModel tests care about the cache-survives-failure
 * behaviour, which is a state machine, and a fake states it far more legibly than stubbed calls.
 */
class FakeProductRepository(
    initialProducts: List<Product> = emptyList(),
) : ProductRepository {

    private val cache = MutableStateFlow(initialProducts)

    /** Result the next [refresh] returns. */
    var nextRefresh: AppResult<List<Product>> = AppResult.Success(emptyList())

    var refreshCount: Int = 0
        private set

    override fun observeFeatured(): Flow<List<Product>> =
        cache.map { products -> featuredProducts(products, label = null) }

    override fun observeProductCount(): Flow<Int> = cache.map { it.size }

    override fun pagedProducts(sort: CatalogueSort): Flow<PagingData<Product>> =
        cache.map { products -> PagingData.from(products.sortedFor(sort)) }

    override fun observeProduct(id: Long): Flow<Product?> =
        cache.map { products -> products.firstOrNull { it.id == id } }

    override suspend fun refresh(): AppResult<Unit> {
        refreshCount++
        return when (val result = nextRefresh) {
            is AppResult.Success -> {
                cache.value = result.value
                AppResult.Success(Unit)
            }
            is AppResult.Failure -> result
        }
    }

    fun succeedWith(products: List<Product>) {
        nextRefresh = AppResult.Success(products)
    }

    fun failWith(error: AppError) {
        nextRefresh = AppResult.Failure(error)
    }
}

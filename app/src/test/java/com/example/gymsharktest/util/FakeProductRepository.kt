package com.example.gymsharktest.util

import com.example.gymsharktest.core.AppError
import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.data.repository.ProductRepository
import com.example.gymsharktest.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    override fun observeProducts(): Flow<List<Product>> = cache.asStateFlow()

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

    override suspend fun productById(id: Long): Product? = cache.value.firstOrNull { it.id == id }

    fun succeedWith(products: List<Product>) {
        nextRefresh = AppResult.Success(products)
    }

    fun failWith(error: AppError) {
        nextRefresh = AppResult.Failure(error)
    }
}

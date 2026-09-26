package com.example.gymsharktest.data.repository

import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.data.remote.ProductRemoteDataSource
import com.example.gymsharktest.model.Product
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Caches the catalogue in memory.
 *
 * This is the step before Room: the cache survives configuration changes and navigation but not
 * process death. Room replaces the [cache] field without the interface, the ViewModels, or the UI
 * changing, at which point paging over the stored rows becomes possible too.
 */
@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val remote: ProductRemoteDataSource,
) : ProductRepository {

    private val cache = MutableStateFlow<List<Product>>(emptyList())

    override fun observeProducts(): Flow<List<Product>> = cache.asStateFlow()

    override suspend fun refresh(): AppResult<Unit> = when (val result = remote.fetchProducts()) {
        is AppResult.Success -> {
            cache.value = result.value
            AppResult.Success(Unit)
        }
        // Deliberately does not clear the cache: stale products beat an empty screen.
        is AppResult.Failure -> result
    }

    override suspend fun productById(id: Long): Product? =
        cache.value.firstOrNull { it.id == id }
}

package com.example.gymsharktest.data.repository

import androidx.paging.PagingData
import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.model.CatalogueSort
import com.example.gymsharktest.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * The only data type the ViewModels know about.
 *
 * Kept as an interface so the ViewModel tests can run against a hand-written fake instead of a
 * database, and so the cache implementation can change without touching the UI.
 */
interface ProductRepository {

    /** In-stock products that carry a merchandising label, in catalogue order. */
    fun observeFeatured(): Flow<List<Product>>

    fun observeProductCount(): Flow<Int>

    /**
     * Pages of the full catalogue. [sort] changes the query, not the stored order, so a refresh
     * can replace the rows without forgetting how the shopper asked to see them.
     */
    fun pagedProducts(sort: CatalogueSort): Flow<PagingData<Product>>

    fun observeProduct(id: Long): Flow<Product?>

    /**
     * Fetches the catalogue and replaces the cache.
     *
     * On failure the existing cache is left intact, so a refresh that fails while offline leaves
     * the user looking at stale products rather than an empty screen.
     */
    suspend fun refresh(): AppResult<Unit>
}

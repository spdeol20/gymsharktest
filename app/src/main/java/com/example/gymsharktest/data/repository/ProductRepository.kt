package com.example.gymsharktest.data.repository

import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * The only data type the ViewModels know about.
 *
 * Kept as an interface so the ViewModel tests can run against a hand-written fake instead of a
 * mocked network stack, and so the cache implementation can change without touching the UI.
 */
interface ProductRepository {

    /** The cached catalogue, re-emitting whenever a refresh replaces it. */
    fun observeProducts(): Flow<List<Product>>

    /**
     * Fetches the catalogue and replaces the cache.
     *
     * On failure the existing cache is left intact, so a refresh that fails while offline leaves
     * the user looking at stale products rather than an empty screen.
     */
    suspend fun refresh(): AppResult<Unit>

    suspend fun productById(id: Long): Product?
}

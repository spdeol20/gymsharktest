package com.example.gymsharktest.data.repository

import androidx.paging.PagingData
import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.model.CartLine
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
     * How many products the grid is showing. A null [labelKey] is the whole catalogue. A shelf key
     * counts every product carrying that label, including ones that are sold out.
     */
    fun observeGridCount(labelKey: String?): Flow<Int>

    /**
     * Pages of the catalogue. [sort] changes the query, not the stored order, so a refresh can
     * replace the rows without forgetting how the shopper asked to see them. A [labelKey] limits
     * the pages to that merchandising label; null keeps the full catalogue.
     */
    fun pagedProducts(sort: CatalogueSort, labelKey: String?): Flow<PagingData<Product>>

    fun observeProduct(id: Long): Flow<Product?>

    /**
     * Fetches the catalogue and replaces the cache.
     *
     * On failure the existing cache is left intact, so a refresh that fails while offline leaves
     * the user looking at stale products rather than an empty screen.
     */
    suspend fun refresh(): AppResult<Unit>

    /** Basket lines newest first. A line whose product has left the catalogue is omitted. */
    fun observeCart(): Flow<List<CartLine>>

    /** Sum of quantities, for the bag badge. */
    fun observeCartCount(): Flow<Int>

    /**
     * Adds [quantity] of [productId] in [size]. The same product and size increases the existing
     * line, capped at [com.example.gymsharktest.model.BasketQuantity.MAX].
     *
     * Returns false when the product is missing, sold out, the size is not an in-stock size, or
     * the quantity is outside the allowed range. Nothing is written in that case.
     */
    suspend fun addToCart(productId: Long, size: String?, quantity: Int): Boolean

    /**
     * Sets the quantity of an existing line. Zero removes it. A quantity outside the allowed
     * range, or a line that is not in the basket, is left unchanged.
     */
    suspend fun setCartQuantity(productId: Long, size: String?, quantity: Int)
}

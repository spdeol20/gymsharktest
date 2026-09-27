package com.example.gymsharktest.util

import androidx.paging.PagingData
import com.example.gymsharktest.core.AppError
import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.data.repository.ProductRepository
import com.example.gymsharktest.model.BasketQuantity
import com.example.gymsharktest.model.CartLine
import com.example.gymsharktest.model.CatalogueSort
import com.example.gymsharktest.model.Product
import com.example.gymsharktest.model.sortedFor
import com.example.gymsharktest.ui.products.shelfKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
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
        cache.map { products ->
            products.filter { it.inStock && it.merchandisingLabels.isNotEmpty() }
        }

    override fun observeProductCount(): Flow<Int> = cache.map { it.size }

    override fun observeGridCount(labelKey: String?): Flow<Int> =
        cache.map { products -> products.matching(labelKey).size }

    override fun pagedProducts(sort: CatalogueSort, labelKey: String?): Flow<PagingData<Product>> =
        cache.map { products -> PagingData.from(products.matching(labelKey).sortedFor(sort)) }

    override fun observeProduct(id: Long): Flow<Product?> =
        cache.map { products -> products.firstOrNull { it.id == id } }

    override suspend fun refresh(): AppResult<Unit> {
        refreshCount++
        return when (val result = nextRefresh) {
            is AppResult.Success -> {
                cache.value = result.value
                AppResult.Success(Unit)
                val ids = result.value.map { it.id }.toSet()
                cart.value = cart.value.filter { it.productId in ids }
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

    private fun List<Product>.matching(labelKey: String?): List<Product> =
    override fun observeCart(): Flow<List<CartLine>> = combine(cache, cart) { products, rows ->
        rows.sortedByDescending { it.addedAt }.mapNotNull { row ->
            val product = products.firstOrNull { it.id == row.productId } ?: return@mapNotNull null
            CartLine(
                product = product,
                size = row.sizeKey.ifEmpty { null },
                quantity = row.quantity,
            )
        }
    }

    override fun observeCartCount(): Flow<Int> = observeCart().map { lines -> lines.sumOf { it.quantity } }

    override suspend fun addToCart(productId: Long, size: String?, quantity: Int): Boolean {
        if (!BasketQuantity.fits(quantity)) return false
        val product = cache.value.firstOrNull { it.id == productId } ?: return false
        val sizeKey = acceptedSizeKey(product, size) ?: return false
        val existing = cart.value.firstOrNull { it.productId == productId && it.sizeKey == sizeKey }
        val nextQuantity = ((existing?.quantity ?: 0) + quantity).coerceAtMost(BasketQuantity.MAX)
        val row = CartRow(
            productId = productId,
            sizeKey = sizeKey,
            quantity = nextQuantity,
            addedAt = existing?.addedAt ?: System.currentTimeMillis(),
        )
        cart.value = cart.value.filterNot { it.productId == productId && it.sizeKey == sizeKey } + row
        return true
    }

    override suspend fun setCartQuantity(productId: Long, size: String?, quantity: Int) {
        val sizeKey = size.orEmpty()
        if (quantity <= 0) {
            cart.value = cart.value.filterNot { it.productId == productId && it.sizeKey == sizeKey }
            return
        }
        if (quantity > BasketQuantity.MAX) return
        if (cart.value.none { it.productId == productId && it.sizeKey == sizeKey }) return
        if (cache.value.none { it.id == productId }) {
            cart.value = cart.value.filterNot { it.productId == productId && it.sizeKey == sizeKey }
            return
        }
        cart.value = cart.value.map { row ->
            if (row.productId == productId && row.sizeKey == sizeKey) row.copy(quantity = quantity) else row
        }
    }

    private fun acceptedSizeKey(product: Product, size: String?): String? {
        if (!product.inStock) return null
        return if (product.sizes.isEmpty()) {
            if (size.isNullOrEmpty()) "" else null
        } else {
            val chosen = size?.takeIf { it.isNotEmpty() } ?: return null
            if (product.sizes.any { it.size == chosen && it.inStock }) chosen else null
        }
    }

    private data class CartRow(
        val productId: Long,
        val sizeKey: String,
        val quantity: Int,
        val addedAt: Long,
    )

        if (labelKey.isNullOrEmpty()) {
            this
        } else {
            filter { product -> product.labels.any { it.shelfKey() == labelKey } }
        }
}

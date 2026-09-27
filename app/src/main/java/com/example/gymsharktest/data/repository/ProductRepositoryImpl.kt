package com.example.gymsharktest.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.data.local.CartDao
import com.example.gymsharktest.data.local.CartLineEntity
import com.example.gymsharktest.data.local.GymsharkDatabase
import com.example.gymsharktest.data.local.ProductDao
import com.example.gymsharktest.data.local.ProductEntityMapper
import com.example.gymsharktest.data.remote.ProductRemoteDataSource
import com.example.gymsharktest.di.IoDispatcher
import com.example.gymsharktest.model.BasketQuantity
import com.example.gymsharktest.model.CartLine
import com.example.gymsharktest.model.CatalogueSort
import com.example.gymsharktest.model.Product
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Fetches the catalogue once per refresh and pages it out of Room.
 *
 * The database survives process death. A failed refresh does not delete the previous rows.
 */
@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val remote: ProductRemoteDataSource,
    private val database: GymsharkDatabase,
    private val dao: ProductDao,
    private val cartDao: CartDao,
    private val mapper: ProductEntityMapper,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ProductRepository {

    override fun observeFeatured(): Flow<List<Product>> =
        dao.observeFeatured()
            .map { rows -> rows.map(mapper::toProduct) }
            .flowOn(io)

    override fun observeProductCount(): Flow<Int> = dao.observeCount()

    override fun observeGridCount(labelKey: String?): Flow<Int> {
        val label = LabelMatch.fromShelfKey(labelKey)
        return if (label == null) {
            dao.observeCount()
        } else {
            dao.observeCountByLabel(label.key, label.text)
        }
    }

    override fun pagedProducts(sort: CatalogueSort, labelKey: String?): Flow<PagingData<Product>> =
        Pager(PAGING) {
            val label = LabelMatch.fromShelfKey(labelKey)
            when {
                label == null -> when (sort) {
                    CatalogueSort.Catalogue -> dao.pagingCatalogue()
                    CatalogueSort.PriceLowToHigh -> dao.pagingPriceAsc()
                    CatalogueSort.PriceHighToLow -> dao.pagingPriceDesc()
                }
                else -> when (sort) {
                    CatalogueSort.Catalogue -> dao.pagingCatalogueByLabel(label.key, label.text)
                    CatalogueSort.PriceLowToHigh -> dao.pagingPriceAscByLabel(label.key, label.text)
                    CatalogueSort.PriceHighToLow -> dao.pagingPriceDescByLabel(label.key, label.text)
                }
            }
        }.flow.map { page -> page.map(mapper::toProduct) }

    override fun observeProduct(id: Long): Flow<Product?> =
        dao.observeById(id)
            .map { row -> row?.let(mapper::toProduct) }
            .flowOn(io)

    override suspend fun refresh(): AppResult<Unit> = withContext(io) {
        when (val result = remote.fetchProducts()) {
            is AppResult.Success -> {
                database.withTransaction {
                    dao.replaceAll(mapper.toEntities(result.value))
                    cartDao.deleteOrphans()
                }
                AppResult.Success(Unit)
            }
            // Deliberately does not clear the table: stale products beat an empty screen.
            is AppResult.Failure -> result
        }
    }

    override fun observeCart(): Flow<List<CartLine>> =
        cartDao.observeLines()
            .map { rows ->
                rows.mapNotNull { row ->
                    val product = row.product?.let(mapper::toProduct) ?: return@mapNotNull null
                    CartLine(
                        product = product,
                        size = row.line.sizeKey.ifEmpty { null },
                        quantity = row.line.quantity,
                    )
                }
            }
            .flowOn(io)

    override fun observeCartCount(): Flow<Int> =
        cartDao.observeQuantity().map { total -> total.coerceAtMost(Int.MAX_VALUE.toLong()).toInt() }

    override suspend fun addToCart(productId: Long, size: String?, quantity: Int): Boolean =
        withContext(io) {
            if (!BasketQuantity.fits(quantity)) return@withContext false
            val product = dao.findById(productId)?.let(mapper::toProduct) ?: return@withContext false
            val sizeKey = acceptedSizeKey(product, size) ?: return@withContext false
            val existing = cartDao.find(productId, sizeKey)
            val nextQuantity = ((existing?.quantity ?: 0) + quantity).coerceAtMost(BasketQuantity.MAX)
            cartDao.upsert(
                CartLineEntity(
                    productId = productId,
                    sizeKey = sizeKey,
                    quantity = nextQuantity,
                    addedAt = existing?.addedAt ?: System.currentTimeMillis(),
                ),
            )
            true
        }

    override suspend fun setCartQuantity(productId: Long, size: String?, quantity: Int) {
        withContext(io) {
            val sizeKey = size.orEmpty()
            if (quantity <= 0) {
                cartDao.delete(productId, sizeKey)
                return@withContext
            }
            if (quantity > BasketQuantity.MAX) return@withContext
            val existing = cartDao.find(productId, sizeKey) ?: return@withContext
            if (dao.findById(productId) == null) {
                cartDao.delete(productId, sizeKey)
                return@withContext
            }
            cartDao.upsert(existing.copy(quantity = quantity))
        }
    }

    /**
     * A product with no sizes accepts only a null or blank size. A product with sizes accepts
     * only a size that is still in stock. Sold-out products are never added.
     */
    private fun acceptedSizeKey(product: Product, size: String?): String? {
        if (!product.inStock) return null
        return if (product.sizes.isEmpty()) {
            if (size.isNullOrEmpty()) "" else null
        } else {
            val chosen = size?.takeIf { it.isNotEmpty() } ?: return null
            if (product.sizes.any { it.size == chosen && it.inStock }) chosen else null
        }
    }

    private data class LabelMatch(val key: String, val text: String) {
        companion object {
            fun fromShelfKey(shelfKey: String?): LabelMatch? {
                if (shelfKey.isNullOrEmpty()) return null
                return if (shelfKey.startsWith(UNKNOWN_PREFIX)) {
                    val text = shelfKey.removePrefix(UNKNOWN_PREFIX)
                    LabelMatch(key = "unknown", text = likeLiteral(jsonStringContent(text)))
                } else {
                    LabelMatch(key = likeLiteral(shelfKey), text = "")
                }
            }

            private const val UNKNOWN_PREFIX = "unknown:"

            /** Encodes [raw] the way kotlinx JSON writes a string, so the LIKE matches the column. */
            private fun jsonStringContent(raw: String): String = buildString {
                for (character in raw) {
                    when (character) {
                        '\\' -> append("\\\\")
                        '"' -> append("\\\"")
                        '\n' -> append("\\n")
                        '\r' -> append("\\r")
                        '\t' -> append("\\t")
                        else -> append(character)
                    }
                }
            }

            /** Stops `%`, `_`, and `\` in a label from acting as LIKE wildcards. */
            private fun likeLiteral(raw: String): String = buildString {
                for (character in raw) {
                    when (character) {
                        '\\', '%', '_' -> append('\\').append(character)
                        else -> append(character)
                    }
                }
            }
        }
    }

    private companion object {
        val PAGING = PagingConfig(
            pageSize = 20,
            initialLoadSize = 40,
            prefetchDistance = 10,
            enablePlaceholders = false,
            // Pages far from the viewport are dropped. The table still holds the full catalogue.
            maxSize = 200,
        )
    }
}

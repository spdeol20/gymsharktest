package com.example.gymsharktest.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.data.local.ProductDao
import com.example.gymsharktest.data.local.ProductEntityMapper
import com.example.gymsharktest.data.remote.ProductRemoteDataSource
import com.example.gymsharktest.di.IoDispatcher
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
    private val dao: ProductDao,
    private val mapper: ProductEntityMapper,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ProductRepository {

    override fun observeFeatured(): Flow<List<Product>> =
        dao.observeFeatured()
            .map { rows -> rows.map(mapper::toProduct) }
            .flowOn(io)

    override fun observeProductCount(): Flow<Int> = dao.observeCount()

    override fun pagedProducts(sort: CatalogueSort): Flow<PagingData<Product>> =
        Pager(PAGING) {
            when (sort) {
                CatalogueSort.Catalogue -> dao.pagingCatalogue()
                CatalogueSort.PriceLowToHigh -> dao.pagingPriceAsc()
                CatalogueSort.PriceHighToLow -> dao.pagingPriceDesc()
            }
        }.flow.map { page -> page.map(mapper::toProduct) }

    override fun observeProduct(id: Long): Flow<Product?> =
        dao.observeById(id)
            .map { row -> row?.let(mapper::toProduct) }
            .flowOn(io)

    override suspend fun refresh(): AppResult<Unit> = withContext(io) {
        when (val result = remote.fetchProducts()) {
            is AppResult.Success -> {
                dao.replaceAll(mapper.toEntities(result.value))
                AppResult.Success(Unit)
            }
            // Deliberately does not clear the table: stale products beat an empty screen.
            is AppResult.Failure -> result
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

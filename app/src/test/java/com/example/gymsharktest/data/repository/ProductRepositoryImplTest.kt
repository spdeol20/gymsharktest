package com.example.gymsharktest.data.repository

import android.app.Application
import android.content.Context
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.gymsharktest.core.AppError
import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.data.local.GymsharkDatabase
import com.example.gymsharktest.data.local.ProductEntityMapper
import com.example.gymsharktest.data.remote.ProductRemoteDataSource
import com.example.gymsharktest.model.Price
import com.example.gymsharktest.model.ProductLabel
import com.example.gymsharktest.util.testProduct
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [31], application = Application::class)
class ProductRepositoryImplTest {

    private val remote: ProductRemoteDataSource = mockk()
    private lateinit var database: GymsharkDatabase
    private lateinit var repository: ProductRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GymsharkDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ProductRepositoryImpl(
            remote = remote,
            database = database,
            dao = database.productDao(),
            cartDao = database.cartDao(),
            mapper = ProductEntityMapper(Json { ignoreUnknownKeys = true }),
            io = UnconfinedTestDispatcher(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `the catalogue starts empty`() = runTest {
        assertEquals(0, database.productDao().count())
    }

    @Test
    fun `a successful refresh stores the fetched products`() = runTest {
        val products = listOf(testProduct(id = 1), testProduct(id = 2))
        coEvery { remote.fetchProducts() } returns AppResult.Success(products)

        val result = repository.refresh()

        assertTrue(result is AppResult.Success)
        assertEquals(2, database.productDao().count())
        assertEquals("Test Product 1", database.productDao().findById(1L)?.title)
    }

    @Test
    fun `a refresh replaces the previous contents rather than appending`() = runTest {
        coEvery { remote.fetchProducts() } returns AppResult.Success(listOf(testProduct(id = 1)))
        repository.refresh()

        coEvery { remote.fetchProducts() } returns AppResult.Success(listOf(testProduct(id = 9)))
        repository.refresh()

        assertEquals(1, database.productDao().count())
        assertEquals(9L, database.productDao().findById(9L)?.id)
        assertEquals(null, database.productDao().findById(1L))
    }

    @Test
    fun `a failed refresh keeps the stored catalogue intact`() = runTest {
        coEvery { remote.fetchProducts() } returns AppResult.Success(listOf(testProduct(id = 1, title = "Speed")))
        repository.refresh()

        coEvery { remote.fetchProducts() } returns AppResult.Failure(AppError.Network)
        val result = repository.refresh()

        assertEquals(AppResult.Failure(AppError.Network), result)
        assertEquals("Speed", database.productDao().findById(1L)?.title)
    }

    @Test
    fun `featured rows are in-stock products with a merchandising label`() = runTest {
        coEvery { remote.fetchProducts() } returns AppResult.Success(
            listOf(
                testProduct(id = 1, labels = listOf(ProductLabel.New)),
                testProduct(id = 2, inStock = false, labels = listOf(ProductLabel.New)),
                testProduct(id = 3, labels = listOf(ProductLabel.RecycledNylon)),
            ),
        )
        repository.refresh()

        assertEquals(listOf(1L), database.productDao().featured().map { it.id })
    }

    @Test
    fun `price ascending keeps catalogue order when prices match`() = runTest {
        val products = listOf(
            testProduct(id = 1, price = Price(amountMinorUnits = 3000)),
            testProduct(id = 2, price = Price(amountMinorUnits = 1000)),
            testProduct(id = 3, price = Price(amountMinorUnits = 1000)),
        )
        database.productDao().replaceAll(
            ProductEntityMapper(Json { ignoreUnknownKeys = true }).toEntities(products),
        )

        val page = database.productDao().pagingPriceAsc().load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 10, placeholdersEnabled = false),
        )

        val ids = (page as PagingSource.LoadResult.Page).data.map { it.id }
        assertEquals(listOf(2L, 3L, 1L), ids)
    }
}

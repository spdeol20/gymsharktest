package com.example.gymsharktest.data.remote

import com.example.gymsharktest.core.AppError
import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.data.mapper.ProductMapper
import com.example.gymsharktest.data.remote.dto.ProductsResponseDto
import com.example.gymsharktest.di.NetworkModule
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Every failure mode has to arrive at the UI as an [AppError], never as an exception: this is the
 * boundary that decides what the retry screen says.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductRemoteDataSourceTest {

    private val json = NetworkModule.provideJson()

    @Test
    fun `a usable payload becomes a success`() = runTest {
        val source = dataSourceReturning(responseWith(VALID_PRODUCT))

        val result = source.fetchProducts()

        assertEquals(1, result.successValue().size)
        assertEquals("Speed Leggings", result.successValue().first().title)
    }

    @Test
    fun `an empty catalogue is a failure rather than an empty success`() = runTest {
        val source = dataSourceReturning(ProductsResponseDto(hits = emptyList()))

        assertEquals(AppError.Empty, source.fetchProducts().failureError())
    }

    @Test
    fun `a payload where every record is unusable is empty, not a parsing failure`() = runTest {
        val source = dataSourceReturning(responseWith(UNUSABLE_PRODUCT))

        assertEquals(AppError.Empty, source.fetchProducts().failureError())
    }

    @Test
    fun `no connection becomes a network error`() = runTest {
        val source = dataSourceThrowing(IOException("no route to host"))

        assertEquals(AppError.Network, source.fetchProducts().failureError())
    }

    @Test
    fun `an unresolvable host becomes a network error`() = runTest {
        val source = dataSourceThrowing(UnknownHostException("cdn.develop.gymshark.com"))

        assertEquals(AppError.Network, source.fetchProducts().failureError())
    }

    @Test
    fun `a socket timeout becomes a timeout error`() = runTest {
        val source = dataSourceThrowing(SocketTimeoutException("timeout"))

        assertEquals(AppError.Timeout, source.fetchProducts().failureError())
    }

    @Test
    fun `the whole-call timeout becomes a timeout error`() = runTest {
        val source = dataSourceThrowing(InterruptedIOException("timeout"))

        assertEquals(AppError.Timeout, source.fetchProducts().failureError())
    }

    @Test
    fun `undecodable json becomes a parsing error`() = runTest {
        val source = dataSourceThrowing(SerializationException("unexpected token"))

        assertEquals(AppError.Parsing, source.fetchProducts().failureError())
    }

    @Test
    fun `a server error status becomes a network error`() = runTest {
        val source = dataSourceThrowing(httpException(500))

        assertEquals(AppError.Network, source.fetchProducts().failureError())
    }

    @Test
    fun `a missing document becomes a network error`() = runTest {
        val source = dataSourceThrowing(httpException(404))

        assertEquals(AppError.Network, source.fetchProducts().failureError())
    }

    @Test
    fun `anything unforeseen becomes an unexpected error rather than a crash`() = runTest {
        val source = dataSourceThrowing(IllegalStateException("something new"))

        assertEquals(AppError.Unexpected, source.fetchProducts().failureError())
    }

    @Test
    fun `cancellation propagates instead of being reported as a failure`() {
        val source = dataSourceThrowing(CancellationException("cancelled"))

        assertThrows(CancellationException::class.java) {
            runBlocking { source.fetchProducts() }
        }
    }

    private fun dataSourceReturning(response: ProductsResponseDto) = dataSource { response }

    private fun dataSourceThrowing(throwable: Throwable) = dataSource { throw throwable }

    private fun dataSource(block: suspend () -> ProductsResponseDto) = ProductRemoteDataSource(
        api = object : ProductApi {
            override suspend fun getProducts(): ProductsResponseDto = block()
        },
        mapper = ProductMapper(json),
        dispatcher = UnconfinedTestDispatcher(),
    )

    private fun responseWith(vararg productJson: String): ProductsResponseDto =
        json.decodeFromString(
            ProductsResponseDto.serializer(),
            """{"hits":[${productJson.joinToString(",")}]}""",
        )

    private fun httpException(code: Int) = HttpException(
        Response.error<ProductsResponseDto>(
            code,
            "error".toResponseBody("text/plain".toMediaType()),
        ),
    )

    private fun <T> AppResult<T>.successValue(): T =
        (this as? AppResult.Success<T>)?.value ?: error("expected a success but was $this")

    private fun AppResult<*>.failureError(): AppError =
        (this as? AppResult.Failure)?.error ?: error("expected a failure but was $this")

    private companion object {
        const val VALID_PRODUCT = """
            {
              "id": 6732609257571,
              "sku": "B3A3E",
              "title": "Speed Leggings",
              "price": 1000,
              "inStock": true
            }
        """

        /** Decodes cleanly but has no id, so the mapper drops it. */
        const val UNUSABLE_PRODUCT = """{ "sku": "NOID", "title": "No Id", "price": 1000 }"""
    }
}

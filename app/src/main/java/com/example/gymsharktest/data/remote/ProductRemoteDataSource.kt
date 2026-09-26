package com.example.gymsharktest.data.remote

import com.example.gymsharktest.core.AppError
import com.example.gymsharktest.core.AppResult
import com.example.gymsharktest.data.mapper.ProductMapper
import com.example.gymsharktest.di.IoDispatcher
import com.example.gymsharktest.model.Product
import java.io.IOException
import java.io.InterruptedIOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/**
 * The single boundary where network and decoding failures stop being exceptions.
 *
 * Everything above this class sees an [AppResult]; no `IOException` or `HttpException` escapes
 * upward, which is what keeps host names and transport details out of anything renderable.
 *
 * The work runs on [dispatcher] rather than the caller's thread because decoding ~1000 records is
 * real CPU work, and a Retrofit `suspend` call resumes on whichever dispatcher its caller is
 * confined to — which for a ViewModel would be the main thread.
 */
class ProductRemoteDataSource @Inject constructor(
    private val api: ProductApi,
    private val mapper: ProductMapper,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {

    suspend fun fetchProducts(): AppResult<List<Product>> = withContext(dispatcher) {
        try {
            val products = mapper.toProducts(api.getProducts())
            if (products.isEmpty()) {
                AppResult.Failure(AppError.Empty)
            } else {
                AppResult.Success(products)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: InterruptedIOException) {
            // Covers SocketTimeoutException and OkHttp's whole-call timeout.
            AppResult.Failure(AppError.Timeout)
        } catch (_: IOException) {
            AppResult.Failure(AppError.Network)
        } catch (_: SerializationException) {
            AppResult.Failure(AppError.Parsing)
        } catch (_: HttpException) {
            // A non-2xx from a static CDN document is a server-side problem, and from the user's
            // point of view indistinguishable from not being able to reach it.
            AppResult.Failure(AppError.Network)
        } catch (_: Exception) {
            AppResult.Failure(AppError.Unexpected)
        }
    }
}

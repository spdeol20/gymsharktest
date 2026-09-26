package com.example.gymsharktest.data.remote

import com.example.gymsharktest.data.remote.dto.ProductsResponseDto
import retrofit2.http.GET

interface ProductApi {

    /**
     * The catalogue is a single static document: there are no page, offset, or cursor parameters,
     * which is why pagination happens locally over the cached copy rather than over the network.
     */
    @GET("training/mock-product-responses/algolia-example-payload.json")
    suspend fun getProducts(): ProductsResponseDto
}

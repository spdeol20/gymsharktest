package com.example.gymsharktest.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * The catalogue response envelope.
 *
 * `hits` is deliberately left as raw [JsonElement]s rather than a `List<ProductDto>`. Decoding the
 * whole array in one pass makes a single malformed record fatal to all ~1000 products; keeping the
 * elements raw lets the mapper decode them one at a time and skip only the ones that fail.
 */
@Serializable
data class ProductsResponseDto(
    val hits: List<JsonElement>? = null,
)

package com.example.gymsharktest.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A single catalogue product as sent over the wire.
 *
 * Every field is nullable with a default. The payload is attacker-controlled input as far as this
 * app is concerned, so the type system must not promise anything the API does not guarantee — the
 * mapper is the single place where those promises get made.
 *
 * `labels` is a raw [JsonElement] because the field is `null` in every sample we have, leaving its
 * real shape unknown; the mapper accepts a string, an array of strings, or an array of objects.
 *
 * `discountPercentage` is intentionally absent: the discount is derived from `price` and
 * `compareAtPrice` so the badge can never contradict the prices shown next to it, which also means
 * the field's wire type does not matter.
 */
@Serializable
data class ProductDto(
    val id: Long? = null,
    val sku: String? = null,
    val title: String? = null,
    val description: String? = null,
    val type: String? = null,
    val colour: String? = null,
    val fit: String? = null,
    val inStock: Boolean? = null,
    val price: Long? = null,
    val compareAtPrice: Long? = null,
    val labels: JsonElement? = null,
    val availableSizes: List<AvailableSizeDto>? = null,
    val featuredMedia: MediaDto? = null,
    val media: List<MediaDto>? = null,
)

package com.example.gymsharktest.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AvailableSizeDto(
    val size: String? = null,
    val inStock: Boolean? = null,
    val inventoryQuantity: Int? = null,
)

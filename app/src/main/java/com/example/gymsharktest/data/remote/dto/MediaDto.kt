package com.example.gymsharktest.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MediaDto(
    val src: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val position: Int? = null,
    val alt: String? = null,
)

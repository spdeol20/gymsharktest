package com.example.gymsharktest.model

import androidx.compose.runtime.Immutable

@Immutable
data class SizeAvailability(
    val size: String,
    val inStock: Boolean,
)

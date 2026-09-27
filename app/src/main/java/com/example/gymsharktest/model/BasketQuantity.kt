package com.example.gymsharktest.model

/** How many of one product and size a basket line may hold. */
object BasketQuantity {
    const val MIN: Int = 1
    const val MAX: Int = 10

    fun fits(quantity: Int): Boolean = quantity in MIN..MAX
}

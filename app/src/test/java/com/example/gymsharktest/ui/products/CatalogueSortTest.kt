package com.example.gymsharktest.ui.products

import com.example.gymsharktest.model.Price
import com.example.gymsharktest.util.testProduct
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogueSortTest {

    private val products = listOf(
        testProduct(id = 1, price = Price(amountMinorUnits = 3000)),
        testProduct(id = 2, price = Price(amountMinorUnits = 1000)),
        testProduct(id = 3, price = Price(amountMinorUnits = 1000)),
        testProduct(id = 4, price = Price(amountMinorUnits = 2000)),
    )

    @Test
    fun `catalogue order is unchanged`() {
        assertEquals(listOf(1L, 2L, 3L, 4L), products.sortedFor(CatalogueSort.Catalogue).map { it.id })
    }

    @Test
    fun `low to high keeps the original order when prices match`() {
        assertEquals(listOf(2L, 3L, 4L, 1L), products.sortedFor(CatalogueSort.PriceLowToHigh).map { it.id })
    }

    @Test
    fun `high to low keeps the original order when prices match`() {
        assertEquals(listOf(1L, 4L, 2L, 3L), products.sortedFor(CatalogueSort.PriceHighToLow).map { it.id })
    }
}
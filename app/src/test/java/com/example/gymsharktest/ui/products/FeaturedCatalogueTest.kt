package com.example.gymsharktest.ui.products

import com.example.gymsharktest.model.ProductLabel
import com.example.gymsharktest.util.testProduct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeaturedCatalogueTest {

    @Test
    fun `chips follow shop order and skip labels nobody is selling`() {
        val products = listOf(
            testProduct(id = 1, labels = listOf(ProductLabel.Popular)),
            testProduct(id = 2, labels = listOf(ProductLabel.New, ProductLabel.RecycledNylon)),
            testProduct(id = 3, labels = listOf(ProductLabel.LimitedEdition)),
            testProduct(id = 4, inStock = false, labels = listOf(ProductLabel.GoingFast)),
        )

        assertEquals(
            listOf(ProductLabel.New, ProductLabel.LimitedEdition, ProductLabel.Popular),
            featuredLabels(products),
        )
    }

    @Test
    fun `a catalogue with no merchandising labels has no shelf`() {
        val products = listOf(
            testProduct(labels = emptyList()),
            testProduct(id = 2, labels = listOf(ProductLabel.RecycledPolyester)),
        )

        assertTrue(featuredLabels(products).isEmpty())
    }

    @Test
    fun `all and a chip keep only the first five in-stock matches`() {
        val soldOutNew = testProduct(id = 1, inStock = false, labels = listOf(ProductLabel.New))
        val fresh = testProduct(id = 2, labels = listOf(ProductLabel.New))
        val limited = testProduct(id = 3, labels = listOf(ProductLabel.LimitedEdition))
        val plain = testProduct(id = 4)
        val products = listOf(soldOutNew, fresh, limited, plain)

        assertEquals(listOf(fresh, limited), featuredProducts(products, label = null))
        assertEquals(listOf(fresh), featuredProducts(products, label = ProductLabel.New))
    }

    @Test
    fun `the featured row stops after five products`() {
        val products = (1..6).map { id ->
            testProduct(id = id.toLong(), labels = listOf(ProductLabel.New))
        }

        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), featuredProducts(products, ProductLabel.New).map { it.id })
    }
}
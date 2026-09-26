package com.example.gymsharktest.model

import com.example.gymsharktest.util.testProduct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductTest {

    @Test
    fun `labels split into merchandising and material lists`() {
        val product = testProduct(
            labels = listOf(
                ProductLabel.New,
                ProductLabel.RecycledNylon,
                ProductLabel.GoingFast,
                ProductLabel.RecycledPolyester,
            ),
        )

        assertEquals(
            listOf(ProductLabel.New, ProductLabel.GoingFast),
            product.merchandisingLabels,
        )
        assertEquals(
            listOf(ProductLabel.RecycledNylon, ProductLabel.RecycledPolyester),
            product.materialLabels,
        )
    }

    @Test
    fun `card prefers discount over a merchandising label`() {
        val product = testProduct(
            price = Price(amountMinorUnits = 1000, compareAtMinorUnits = 2000),
            labels = listOf(ProductLabel.New),
        )

        assertEquals(50, product.cardDiscountPercent)
        assertNull(product.cardLabel)
    }

    @Test
    fun `card shows one merchandising label when there is no discount`() {
        val product = testProduct(
            labels = listOf(ProductLabel.RecycledNylon, ProductLabel.New, ProductLabel.GoingFast),
        )

        assertNull(product.cardDiscountPercent)
        assertEquals(ProductLabel.New, product.cardLabel)
    }

    @Test
    fun `sold out suppresses both discount and merchandising labels on the card`() {
        val product = testProduct(
            inStock = false,
            price = Price(amountMinorUnits = 1000, compareAtMinorUnits = 2000),
            labels = listOf(ProductLabel.GoingFast),
        )

        assertNull(product.cardDiscountPercent)
        assertNull(product.cardLabel)
    }

    @Test
    fun `low stock is true when one or two sizes remain`() {
        val twoLeft = testProduct(
            sizes = listOf(
                SizeAvailability("S", inStock = true),
                SizeAvailability("M", inStock = true),
                SizeAvailability("L", inStock = false),
            ),
        )
        val oneLeft = testProduct(
            sizes = listOf(
                SizeAvailability("S", inStock = false),
                SizeAvailability("M", inStock = true),
            ),
        )

        assertTrue(twoLeft.isLowStock)
        assertTrue(oneLeft.isLowStock)
        assertEquals(2, twoLeft.remainingSizeCount)
        assertEquals(1, oneLeft.remainingSizeCount)
    }

    @Test
    fun `low stock is false when more than two sizes remain or the product is sold out`() {
        val plenty = testProduct(
            sizes = listOf(
                SizeAvailability("S", inStock = true),
                SizeAvailability("M", inStock = true),
                SizeAvailability("L", inStock = true),
            ),
        )
        val soldOut = testProduct(
            inStock = false,
            sizes = listOf(SizeAvailability("M", inStock = false)),
        )
        val noSizeData = testProduct(sizes = emptyList())

        assertFalse(plenty.isLowStock)
        assertFalse(soldOut.isLowStock)
        assertFalse(noSizeData.isLowStock)
    }
}

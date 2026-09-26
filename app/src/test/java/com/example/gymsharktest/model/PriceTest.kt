package com.example.gymsharktest.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceTest {

    @Test
    fun `a price with no compare-at is not discounted`() {
        val price = Price(amountMinorUnits = 1000L)

        assertFalse(price.isDiscounted)
        assertNull(price.discountPercent)
    }

    @Test
    fun `a compare-at above the price is a discount`() {
        val price = Price(amountMinorUnits = 1000L, compareAtMinorUnits = 2000L)

        assertTrue(price.isDiscounted)
        assertEquals(50, price.discountPercent)
    }

    @Test
    fun `discount percentage is rounded to the nearest whole percent`() {
        val price = Price(amountMinorUnits = 6700L, compareAtMinorUnits = 10000L)

        assertEquals(33, price.discountPercent)
    }

    @Test
    fun `a compare-at equal to the price is not a discount`() {
        val price = Price(amountMinorUnits = 1000L, compareAtMinorUnits = 1000L)

        assertFalse(price.isDiscounted)
        assertNull(price.discountPercent)
    }

    @Test
    fun `a compare-at below the price is ignored rather than shown as a negative saving`() {
        val price = Price(amountMinorUnits = 1000L, compareAtMinorUnits = 500L)

        assertFalse(price.isDiscounted)
        assertNull(price.discountPercent)
    }

    @Test
    fun `a zero compare-at cannot divide by zero`() {
        val price = Price(amountMinorUnits = 1000L, compareAtMinorUnits = 0L)

        assertNull(price.discountPercent)
    }

    @Test
    fun `a saving too small to round up is still reported as one percent`() {
        val price = Price(amountMinorUnits = 9999L, compareAtMinorUnits = 10000L)

        assertTrue(price.isDiscounted)
        assertEquals(1, price.discountPercent)
    }

    @Test
    fun `a near-total saving is capped below one hundred percent`() {
        val price = Price(amountMinorUnits = 1L, compareAtMinorUnits = 100000L)

        assertEquals(99, price.discountPercent)
    }
}

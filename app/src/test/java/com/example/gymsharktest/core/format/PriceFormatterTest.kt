package com.example.gymsharktest.core.format

import com.example.gymsharktest.model.Price
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PriceFormatterTest {

    private val defaultLocale = Locale.getDefault()
    private val formatter = PriceFormatter()

    @After
    fun restoreLocale() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `minor units are converted to major units`() {
        assertEquals("£10.00", formatter.format(1000L))
    }

    @Test
    fun `amounts that are not whole pounds keep both decimal places`() {
        assertEquals("£1.99", formatter.format(199L))
        assertEquals("£0.05", formatter.format(5L))
    }

    @Test
    fun `zero is formatted rather than hidden`() {
        assertEquals("£0.00", formatter.format(0L))
    }

    @Test
    fun `thousands are grouped`() {
        assertEquals("£1,234.56", formatter.format(123456L))
    }

    @Test
    fun `a price object formats its own amount`() {
        assertEquals("£10.00", formatter.format(Price(amountMinorUnits = 1000L)))
    }

    @Test
    fun `compare-at price is formatted only when the product is discounted`() {
        val discounted = Price(amountMinorUnits = 1000L, compareAtMinorUnits = 2000L)
        val fullPrice = Price(amountMinorUnits = 1000L, compareAtMinorUnits = 1000L)
        val noCompareAt = Price(amountMinorUnits = 1000L)

        assertEquals("£20.00", formatter.formatCompareAt(discounted))
        assertNull(formatter.formatCompareAt(fullPrice))
        assertNull(formatter.formatCompareAt(noCompareAt))
    }

    @Test
    fun `output does not depend on the device locale`() {
        Locale.setDefault(Locale.GERMANY)

        assertEquals("£10.00", PriceFormatter().format(1000L))
    }
}

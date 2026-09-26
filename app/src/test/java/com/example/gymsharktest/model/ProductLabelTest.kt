package com.example.gymsharktest.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductLabelTest {

    @Test
    fun `null and blank values produce no label`() {
        assertNull(ProductLabel.from(null))
        assertNull(ProductLabel.from(""))
        assertNull(ProductLabel.from("   "))
        assertNull(ProductLabel.from("\n\t"))
    }

    @Test
    fun `known labels are recognised regardless of case`() {
        assertEquals(ProductLabel.New, ProductLabel.from("new"))
        assertEquals(ProductLabel.New, ProductLabel.from("NEW"))
        assertEquals(ProductLabel.New, ProductLabel.from("New"))
    }

    @Test
    fun `known labels are recognised regardless of separator or padding`() {
        assertEquals(ProductLabel.New, ProductLabel.from("  new in  "))
        assertEquals(ProductLabel.New, ProductLabel.from("new-in"))
        assertEquals(ProductLabel.New, ProductLabel.from("NEW_IN"))
        assertEquals(ProductLabel.ComingSoon, ProductLabel.from("Coming Soon"))
        assertEquals(ProductLabel.ComingSoon, ProductLabel.from("coming-soon"))
        assertEquals(ProductLabel.BackInStock, ProductLabel.from("back in stock"))
        assertEquals(ProductLabel.BackInStock, ProductLabel.from("Restocked"))
        assertEquals(ProductLabel.Sale, ProductLabel.from("SALE"))
        assertEquals(ProductLabel.Sale, ProductLabel.from("on sale"))
    }

    @Test
    fun `an unrecognised label is preserved rather than dropped`() {
        val label = ProductLabel.from("Limited Edition")

        assertEquals(ProductLabel.Unknown("Limited Edition"), label)
    }

    @Test
    fun `an unrecognised label keeps its original casing`() {
        assertEquals(ProductLabel.Unknown("BLACK FRIDAY"), ProductLabel.from("BLACK FRIDAY"))
    }

    @Test
    fun `whitespace inside an unrecognised label is collapsed, not removed`() {
        assertEquals(ProductLabel.Unknown("Limited Edition"), ProductLabel.from("Limited\n   Edition"))
    }

    @Test
    fun `control characters are stripped from an unrecognised label`() {
        val label = ProductLabel.from("Limited\u0000 Edition\u0007")

        assertEquals(ProductLabel.Unknown("Limited Edition"), label)
    }

    @Test
    fun `control characters do not stop a known label being recognised`() {
        assertEquals(ProductLabel.New, ProductLabel.from("new\u0000"))
    }

    @Test
    fun `an overlong label is truncated so it cannot break the badge layout`() {
        val label = ProductLabel.from("A ridiculously long marketing label that would never fit")

        assertTrue(label is ProductLabel.Unknown)
        val text = (label as ProductLabel.Unknown).text
        assertTrue(text.length <= ProductLabel.MAX_UNKNOWN_LENGTH)
        assertTrue(text.endsWith("\u2026"))
    }

    @Test
    fun `a label exactly at the limit is not truncated`() {
        val exact = "x".repeat(ProductLabel.MAX_UNKNOWN_LENGTH)

        assertEquals(ProductLabel.Unknown(exact), ProductLabel.from(exact))
    }

    @Test
    fun `a list of raw labels maps to known labels with blanks dropped`() {
        val raw = listOf("New", null, "  ", "Limited Edition", "SALE")

        val labels = raw.mapNotNull(ProductLabel::from)

        assertEquals(
            listOf(ProductLabel.New, ProductLabel.Unknown("Limited Edition"), ProductLabel.Sale),
            labels,
        )
    }
}

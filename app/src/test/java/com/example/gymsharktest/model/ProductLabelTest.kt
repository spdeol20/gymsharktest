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
        assertEquals(ProductLabel.GoingFast, ProductLabel.from("going-fast"))
        assertEquals(ProductLabel.LimitedEdition, ProductLabel.from("Limited Edition"))
        assertEquals(ProductLabel.LimitedEdition, ProductLabel.from("limited-edition"))
        assertEquals(ProductLabel.Popular, ProductLabel.from("popular"))
        assertEquals(ProductLabel.RecycledNylon, ProductLabel.from("recycled-nylon"))
        assertEquals(ProductLabel.RecycledPolyester, ProductLabel.from("recycled polyester"))
    }

    @Test
    fun `known merchandising and material labels report the right kind`() {
        assertEquals(ProductLabel.Kind.Merchandising, ProductLabel.GoingFast.kind)
        assertEquals(ProductLabel.Kind.Merchandising, ProductLabel.Unknown("FLASH SALE").kind)
        assertEquals(ProductLabel.Kind.Material, ProductLabel.RecycledNylon.kind)
        assertEquals(ProductLabel.Kind.Material, ProductLabel.RecycledPolyester.kind)
    }

    @Test
    fun `an unrecognised label is preserved rather than dropped`() {
        val label = ProductLabel.from("Flash Sale")

        assertEquals(ProductLabel.Unknown("Flash Sale"), label)
    }

    @Test
    fun `unrecognised hyphenated labels become readable display text`() {
        assertEquals(ProductLabel.Unknown("FLASH SALE"), ProductLabel.from("FLASH-SALE"))
    }

    @Test
    fun `an unrecognised label keeps its original casing`() {
        assertEquals(ProductLabel.Unknown("BLACK FRIDAY"), ProductLabel.from("BLACK FRIDAY"))
    }

    @Test
    fun `whitespace inside an unrecognised label is collapsed, not removed`() {
        assertEquals(ProductLabel.Unknown("Flash Sale"), ProductLabel.from("Flash\n   Sale"))
    }

    @Test
    fun `control characters are stripped from an unrecognised label`() {
        val label = ProductLabel.from("Flash\u0000 Sale\u0007")

        assertEquals(ProductLabel.Unknown("Flash Sale"), label)
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
        val raw = listOf("New", null, "  ", "Flash Sale", "SALE")

        val labels = raw.mapNotNull(ProductLabel::from)

        assertEquals(
            listOf(ProductLabel.New, ProductLabel.Unknown("Flash Sale"), ProductLabel.Sale),
            labels,
        )
    }
}

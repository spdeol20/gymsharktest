package com.example.gymsharktest.core.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageUrlTest {

    @Test
    fun `an https url is accepted`() {
        val url = "https://cdn.shopify.com/s/files/product.jpg?v=1649254794"

        assertEquals(url, validatedImageUrl(url))
    }

    @Test
    fun `surrounding whitespace is trimmed`() {
        assertEquals(
            "https://cdn.shopify.com/a.jpg",
            validatedImageUrl("  https://cdn.shopify.com/a.jpg  "),
        )
    }

    @Test
    fun `null, empty, and blank urls are rejected`() {
        assertNull(validatedImageUrl(null))
        assertNull(validatedImageUrl(""))
        assertNull(validatedImageUrl("   "))
    }

    @Test
    fun `plain http is rejected so no image loads over cleartext`() {
        assertNull(validatedImageUrl("http://cdn.shopify.com/a.jpg"))
    }

    @Test
    fun `other protocols are rejected`() {
        assertNull(validatedImageUrl("ftp://cdn.shopify.com/a.jpg"))
        assertNull(validatedImageUrl("file:///sdcard/a.jpg"))
        assertNull(validatedImageUrl("javascript:alert(1)"))
    }

    @Test
    fun `relative urls are rejected`() {
        assertNull(validatedImageUrl("/s/files/product.jpg"))
        assertNull(validatedImageUrl("product.jpg"))
    }

    @Test
    fun `a url with no host is rejected`() {
        assertNull(validatedImageUrl("https:///no-host.jpg"))
    }

    @Test
    fun `an unparseable url is rejected rather than thrown`() {
        assertNull(validatedImageUrl("not a url at all"))
        assertNull(validatedImageUrl("https://cdn.shopify.com/a b c<>.jpg"))
    }

    @Test
    fun `scheme casing does not matter`() {
        assertEquals("HTTPS://cdn.shopify.com/a.jpg", validatedImageUrl("HTTPS://cdn.shopify.com/a.jpg"))
    }

    @Test
    fun `a width parameter is appended to a url that already has a query`() {
        val sized = "https://cdn.shopify.com/a.jpg?v=1649254794".withCdnWidth(400)

        assertEquals("https://cdn.shopify.com/a.jpg?v=1649254794&width=400", sized)
    }

    @Test
    fun `a width parameter starts a query on a url that has none`() {
        assertEquals(
            "https://cdn.shopify.com/a.jpg?width=400",
            "https://cdn.shopify.com/a.jpg".withCdnWidth(400),
        )
    }

    @Test
    fun `an existing width parameter is left alone`() {
        val url = "https://cdn.shopify.com/a.jpg?width=800"

        assertEquals(url, url.withCdnWidth(400))
    }

    @Test
    fun `a non-positive width is ignored`() {
        val url = "https://cdn.shopify.com/a.jpg"

        assertEquals(url, url.withCdnWidth(0))
        assertEquals(url, url.withCdnWidth(-1))
    }
}

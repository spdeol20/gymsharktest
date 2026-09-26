package com.example.gymsharktest.data.mapper

import com.example.gymsharktest.data.remote.dto.ProductsResponseDto
import com.example.gymsharktest.di.NetworkModule
import com.example.gymsharktest.model.Product
import com.example.gymsharktest.model.ProductLabel
import com.example.gymsharktest.model.SizeAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Uses the production `Json` configuration rather than a locally built one, so a change to
 * `ignoreUnknownKeys` or strictness in [NetworkModule] cannot quietly diverge from what is tested.
 */
class ProductMapperTest {

    private val json = NetworkModule.provideJson()
    private val mapper = ProductMapper(json)

    @Test
    fun `an empty response produces no products`() {
        assertTrue(mapper.toProducts(ProductsResponseDto(hits = emptyList())).isEmpty())
    }

    @Test
    fun `a response with null hits produces no products`() {
        assertTrue(mapper.toProducts(ProductsResponseDto(hits = null)).isEmpty())
    }

    @Test
    fun `usable records survive and unusable ones are skipped`() {
        val products = sampleProducts()

        assertEquals(
            listOf(6732609257571L, 1002L, 1003L, 1004L, 1005L, 1010L, 1011L, 1012L, 1013L),
            products.map(Product::id),
        )
    }

    @Test
    fun `a record missing an id is skipped`() {
        assertNull(sampleProducts().find { it.sku == "NOID" })
    }

    @Test
    fun `a record with a blank title is skipped`() {
        assertNull(sampleProducts().find { it.sku == "NOTITLE" })
    }

    @Test
    fun `a record missing a price is skipped because it cannot be sold`() {
        assertNull(sampleProducts().find { it.sku == "NOPRICE" })
    }

    @Test
    fun `a record whose field changed type is skipped without failing the whole response`() {
        val products = sampleProducts()

        assertNull("the malformed record should be gone", products.find { it.sku == "BADTYPE" })
        assertEquals("its siblings should be unaffected", 9, products.size)
    }

    @Test
    fun `fields the app does not model are ignored`() {
        val minimal = productWithSku("MINIMAL")

        assertEquals("Minimal Product", minimal.title)
        assertEquals(0L, minimal.price.amountMinorUnits)
    }

    @Test
    fun `the featured image comes first and duplicate urls are removed`() {
        val leggings = productWithSku("B3A3E")

        assertEquals(
            listOf(
                "https://cdn.shopify.com/s/files/1/1326/4923/products/featured.jpg?v=1649254794",
                "https://cdn.shopify.com/s/files/second.jpg",
                "https://cdn.shopify.com/s/files/third.jpg",
            ),
            leggings.images.map { it.url },
        )
        assertEquals(leggings.images.first(), leggings.featuredImage)
    }

    @Test
    fun `image dimensions are carried through`() {
        val image = productWithSku("B3A3E").images.first()

        assertEquals(1692, image.widthPx)
        assertEquals(2018, image.heightPx)
    }

    @Test
    fun `a product with no media at all simply has no images`() {
        val product = productWithSku("NOIMG")

        assertTrue(product.images.isEmpty())
        assertFalse(product.hasImage)
        assertNull(product.featuredImage)
    }

    @Test
    fun `insecure, blank, and malformed image urls are all rejected`() {
        val product = productWithSku("BADIMG")

        assertTrue("unusable media leaked through: ${product.images}", product.images.isEmpty())
    }

    @Test
    fun `sizes are upper-cased and keep their stock flags`() {
        val sizes = productWithSku("B3A3E").sizes

        assertEquals(
            listOf(
                SizeAvailability("XS", inStock = true),
                SizeAvailability("S", inStock = false),
                SizeAvailability("L", inStock = true),
            ),
            sizes,
        )
    }

    @Test
    fun `availability falls back to inventory quantity when the flag is absent`() {
        val large = productWithSku("B3A3E").sizes.first { it.size == "L" }

        assertTrue(large.inStock)
    }

    @Test
    fun `available sizes expose only the ones in stock`() {
        assertEquals(
            listOf("XS", "L"),
            productWithSku("B3A3E").availableSizes.map(SizeAvailability::size),
        )
    }

    @Test
    fun `a compare-at price above the price becomes a discount`() {
        val price = productWithSku("B3A3E").price

        assertEquals(1000L, price.amountMinorUnits)
        assertEquals(2000L, price.compareAtMinorUnits)
        assertTrue(price.isDiscounted)
        assertEquals(50, price.discountPercent)
    }

    @Test
    fun `string labels are parsed, blanks dropped, and duplicates removed`() {
        val labels = productWithSku("LABELSTR").labels

        assertEquals(
            listOf(
                ProductLabel.New,
                ProductLabel.LimitedEdition,
                ProductLabel.ComingSoon,
            ),
            labels,
        )
    }

    @Test
    fun `object labels are parsed from whichever property carries the text`() {
        val labels = productWithSku("LABELOBJ").labels

        assertEquals(listOf(ProductLabel.BackInStock, ProductLabel.Sale), labels)
    }

    @Test
    fun `a null labels field produces no labels`() {
        assertTrue(productWithSku("NOIMG").labels.isEmpty())
    }

    @Test
    fun `stock falls back to the product flag only when there are no sizes`() {
        assertFalse(productWithSku("MINIMAL").inStock)
        assertFalse(productWithSku("NOIMG").inStock)
        assertTrue(productWithSku("BADIMG").inStock)
    }

    @Test
    fun `sizes win when the product flag says in stock but every size is gone`() {
        val product = productWithSku("STALEFLAG")

        assertFalse(product.inStock)
        assertTrue(product.availableSizes.isEmpty())
    }

    @Test
    fun `sizes win when the product flag says sold out but a size is available`() {
        val product = productWithSku("LIESOLD")

        assertTrue(product.inStock)
        assertEquals(listOf("M"), product.availableSizes.map(SizeAvailability::size))
    }

    @Test
    fun `a product with every size out of stock is sold out`() {
        val product = productWithSku("B1A2J")

        assertFalse(product.inStock)
        assertEquals(
            listOf(
                SizeAvailability("XS", inStock = false),
                SizeAvailability("S", inStock = false),
            ),
            product.sizes,
        )
    }

    @Test
    fun `optional text fields become null rather than empty strings`() {
        val product = productWithSku("BADIMG")

        assertNull(product.colour)
        assertNull(product.type)
    }

    @Test
    fun `the description html is carried through untouched for the renderer to handle`() {
        assertEquals(
            "<p><strong>RUN WITH IT</strong></p>",
            productWithSku("B3A3E").descriptionHtml,
        )
    }

    private fun productWithSku(sku: String): Product =
        sampleProducts().first { it.sku == sku }

    private fun sampleProducts(): List<Product> = mapper.toProducts(sampleResponse())

    private fun sampleResponse(): ProductsResponseDto {
        val raw = requireNotNull(javaClass.getResourceAsStream("/catalogue-sample.json")) {
            "catalogue-sample.json is missing from test resources"
        }.bufferedReader().use { it.readText() }

        return json.decodeFromString(ProductsResponseDto.serializer(), raw)
    }
}

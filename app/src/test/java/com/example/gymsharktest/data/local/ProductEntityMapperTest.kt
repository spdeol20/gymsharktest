package com.example.gymsharktest.data.local

import com.example.gymsharktest.model.Price
import com.example.gymsharktest.model.ProductImage
import com.example.gymsharktest.model.ProductLabel
import com.example.gymsharktest.model.SizeAvailability
import com.example.gymsharktest.util.testProduct
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductEntityMapperTest {

    private val mapper = ProductEntityMapper(Json { ignoreUnknownKeys = true })

    @Test
    fun `a product survives a trip through a row`() {
        val product = testProduct(
            id = 7,
            title = "Speed Leggings",
            price = Price(amountMinorUnits = 4500, compareAtMinorUnits = 6000),
            labels = listOf(ProductLabel.New, ProductLabel.RecycledNylon, ProductLabel.Unknown("Studio")),
            images = listOf(ProductImage(url = "https://cdn.example.com/7.jpg", widthPx = 800, heightPx = 1000)),
            sizes = listOf(
                SizeAvailability("S", inStock = true),
                SizeAvailability("M", inStock = false),
            ),
        )

        val restored = mapper.toProduct(mapper.toEntities(listOf(product)).single())

        assertEquals(product, restored)
        assertEquals(0, mapper.toEntities(listOf(product)).single().position)
    }

    @Test
    fun `a corrupt json column becomes an empty list and the rest of the row still reads`() {
        val entity = mapper.toEntities(listOf(testProduct(title = "Apex"))).single()
            .copy(labelsJson = "{", imagesJson = "nope", sizesJson = "")

        val restored = mapper.toProduct(entity)

        assertEquals("Apex", restored.title)
        assertTrue(restored.labels.isEmpty())
        assertTrue(restored.images.isEmpty())
        assertTrue(restored.sizes.isEmpty())
    }
}

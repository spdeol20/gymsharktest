package com.example.gymsharktest.data.mapper

import com.example.gymsharktest.core.image.validatedImageUrl
import com.example.gymsharktest.data.remote.dto.AvailableSizeDto
import com.example.gymsharktest.data.remote.dto.MediaDto
import com.example.gymsharktest.data.remote.dto.ProductDto
import com.example.gymsharktest.data.remote.dto.ProductsResponseDto
import com.example.gymsharktest.model.Price
import com.example.gymsharktest.model.Product
import com.example.gymsharktest.model.ProductImage
import com.example.gymsharktest.model.ProductLabel
import com.example.gymsharktest.model.SizeAvailability
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Turns the wire payload into validated [Product]s.
 *
 * Tolerance is per record, not per response: a product that cannot be decoded or that is missing
 * something it cannot be sold without is skipped, and its ~999 siblings still reach the screen.
 * A catalogue that renders 999 products is a far better outcome than an error screen.
 */
class ProductMapper @Inject constructor(
    private val json: Json,
) {

    fun toProducts(response: ProductsResponseDto): List<Product> =
        response.hits.orEmpty().mapNotNull(::toProduct)

    private fun toProduct(element: JsonElement): Product? = decode(element)?.toProduct()

    private fun decode(element: JsonElement): ProductDto? = try {
        json.decodeFromJsonElement(ProductDto.serializer(), element)
    } catch (_: SerializationException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    /**
     * Returns null when a record is unusable. An id identifies it, a title names it, and a price
     * is required to sell it; anything else can sensibly be absent.
     */
    private fun ProductDto.toProduct(): Product? {
        val id = id?.takeIf { it > 0L } ?: return null
        val title = title.nonBlank() ?: return null
        val amount = price?.takeIf { it >= 0L } ?: return null

        val sizes = availableSizes.orEmpty().mapNotNull { it.toSizeAvailability() }

        return Product(
            id = id,
            sku = sku.nonBlank().orEmpty(),
            title = title,
            colour = colour.nonBlank(),
            type = type.nonBlank(),
            descriptionHtml = description.orEmpty(),
            price = Price(
                amountMinorUnits = amount,
                compareAtMinorUnits = compareAtPrice?.takeIf { it > 0L },
            ),
            inStock = inStock ?: sizes.any { it.inStock },
            labels = parseLabels(labels),
            images = parseImages(),
            sizes = sizes,
        )
    }

    /** Featured image first, then the rest in the order the CDN positions them. */
    private fun ProductDto.parseImages(): List<ProductImage> {
        val ordered = buildList {
            featuredMedia?.let(::add)
            media.orEmpty().sortedBy { it.position ?: Int.MAX_VALUE }.forEach(::add)
        }
        return ordered.mapNotNull { it.toProductImage() }.distinctBy { it.url }
    }

    private fun MediaDto.toProductImage(): ProductImage? {
        val url = validatedImageUrl(src) ?: return null
        return ProductImage(
            url = url,
            widthPx = width?.takeIf { it > 0 },
            heightPx = height?.takeIf { it > 0 },
        )
    }

    private fun AvailableSizeDto.toSizeAvailability(): SizeAvailability? {
        val size = size.nonBlank() ?: return null
        return SizeAvailability(
            size = size.uppercase(),
            inStock = inStock ?: ((inventoryQuantity ?: 0) > 0),
        )
    }

    /**
     * The shape of `labels` is unknown — it is null throughout the sample payload — so a string,
     * an array of strings, and an array of objects are all accepted.
     */
    private fun parseLabels(element: JsonElement?): List<ProductLabel> {
        val parsed = when {
            element == null || element == JsonNull -> emptyList()
            element is JsonArray -> element.mapNotNull(::toLabel)
            else -> listOfNotNull(toLabel(element))
        }
        return parsed.distinct()
    }

    private fun toLabel(element: JsonElement): ProductLabel? = when (element) {
        is JsonPrimitive -> if (element.isString) ProductLabel.from(element.content) else null
        is JsonObject -> LABEL_KEYS.firstNotNullOfOrNull { key ->
            val value = element[key]
            if (value is JsonPrimitive && value.isString) ProductLabel.from(value.content) else null
        }
        else -> null
    }

    private fun String?.nonBlank(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private companion object {
        /** Property names a label object might use, in order of preference. */
        val LABEL_KEYS = listOf("name", "label", "title", "text", "value")
    }
}

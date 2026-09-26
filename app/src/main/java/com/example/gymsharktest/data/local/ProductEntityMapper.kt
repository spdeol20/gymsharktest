package com.example.gymsharktest.data.local

import com.example.gymsharktest.model.Price
import com.example.gymsharktest.model.Product
import com.example.gymsharktest.model.ProductImage
import com.example.gymsharktest.model.ProductLabel
import com.example.gymsharktest.model.SizeAvailability
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Maps between the domain product and its Room row.
 *
 * A JSON column that cannot be read becomes an empty list. The scalar columns still render, and
 * the raw text is never logged.
 */
class ProductEntityMapper @Inject constructor(
    private val json: Json,
) {
    private val imageList = ListSerializer(StoredImage.serializer())
    private val sizeList = ListSerializer(StoredSize.serializer())
    private val labelList = ListSerializer(StoredLabel.serializer())

    fun toEntities(products: List<Product>): List<ProductEntity> =
        products.mapIndexed { index, product -> product.toEntity(index) }

    fun toProduct(entity: ProductEntity): Product {
        val decodedLabels = decode(labelList, entity.labelsJson).mapNotNull { it.toLabel() }
        return Product(
            id = entity.id,
            sku = entity.sku,
            title = entity.title,
            colour = entity.colour,
            type = entity.type,
            descriptionHtml = entity.descriptionHtml,
            price = Price(
                amountMinorUnits = entity.amountMinorUnits,
                compareAtMinorUnits = entity.compareAtMinorUnits,
            ),
            inStock = entity.inStock,
            labels = decodedLabels,
            images = decode(imageList, entity.imagesJson).map { stored ->
                ProductImage(
                    url = stored.url,
                    widthPx = stored.widthPx,
                    heightPx = stored.heightPx,
                )
            },
            sizes = decode(sizeList, entity.sizesJson).map { stored ->
                SizeAvailability(size = stored.size, inStock = stored.inStock)
            },
        )
    }

    private fun Product.toEntity(position: Int): ProductEntity = ProductEntity(
        id = id,
        sku = sku,
        title = title,
        colour = colour,
        type = type,
        descriptionHtml = descriptionHtml,
        amountMinorUnits = price.amountMinorUnits,
        compareAtMinorUnits = price.compareAtMinorUnits,
        inStock = inStock,
        labelsJson = json.encodeToString(labelList, labels.map { it.toStored() }),
        imagesJson = json.encodeToString(
            imageList,
            images.map { image ->
                StoredImage(url = image.url, widthPx = image.widthPx, heightPx = image.heightPx)
            },
        ),
        sizesJson = json.encodeToString(
            sizeList,
            sizes.map { size -> StoredSize(size = size.size, inStock = size.inStock) },
        ),
        hasMerchandisingLabel = merchandisingLabels.isNotEmpty(),
        position = position,
    )

    private fun <T> decode(serializer: kotlinx.serialization.KSerializer<List<T>>, raw: String): List<T> =
        try {
            json.decodeFromString(serializer, raw)
        } catch (_: SerializationException) {
            emptyList()
        } catch (_: IllegalArgumentException) {
            emptyList()
        }
}

private fun ProductLabel.toStored(): StoredLabel = when (this) {
    ProductLabel.New -> StoredLabel("new")
    ProductLabel.ComingSoon -> StoredLabel("coming-soon")
    ProductLabel.BackInStock -> StoredLabel("back-in-stock")
    ProductLabel.Sale -> StoredLabel("sale")
    ProductLabel.GoingFast -> StoredLabel("going-fast")
    ProductLabel.LimitedEdition -> StoredLabel("limited-edition")
    ProductLabel.Popular -> StoredLabel("popular")
    ProductLabel.RecycledNylon -> StoredLabel("recycled-nylon")
    ProductLabel.RecycledPolyester -> StoredLabel("recycled-polyester")
    is ProductLabel.Unknown -> StoredLabel("unknown", text)
}

private fun StoredLabel.toLabel(): ProductLabel? = when (key) {
    "new" -> ProductLabel.New
    "coming-soon" -> ProductLabel.ComingSoon
    "back-in-stock" -> ProductLabel.BackInStock
    "sale" -> ProductLabel.Sale
    "going-fast" -> ProductLabel.GoingFast
    "limited-edition" -> ProductLabel.LimitedEdition
    "popular" -> ProductLabel.Popular
    "recycled-nylon" -> ProductLabel.RecycledNylon
    "recycled-polyester" -> ProductLabel.RecycledPolyester
    "unknown" -> text?.takeIf { it.isNotBlank() }?.let(ProductLabel::Unknown)
    else -> null
}

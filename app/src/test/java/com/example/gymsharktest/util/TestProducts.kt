package com.example.gymsharktest.util

import com.example.gymsharktest.model.Price
import com.example.gymsharktest.model.Product
import com.example.gymsharktest.model.ProductImage
import com.example.gymsharktest.model.ProductLabel
import com.example.gymsharktest.model.SizeAvailability

/** Minimal valid product, so each test only states the field it actually cares about. */
fun testProduct(
    id: Long = 1L,
    sku: String = "SKU-$id",
    title: String = "Test Product $id",
    colour: String? = "Black",
    type: String? = "Leggings",
    descriptionHtml: String = "<p>Description</p>",
    price: Price = Price(amountMinorUnits = 1000),
    inStock: Boolean = true,
    labels: List<ProductLabel> = emptyList(),
    images: List<ProductImage> = listOf(ProductImage(url = "https://cdn.example.com/$id.jpg")),
    sizes: List<SizeAvailability> = listOf(SizeAvailability(size = "M", inStock = true)),
): Product = Product(
    id = id,
    sku = sku,
    title = title,
    colour = colour,
    type = type,
    descriptionHtml = descriptionHtml,
    price = price,
    inStock = inStock,
    labels = labels,
    images = images,
    sizes = sizes,
)

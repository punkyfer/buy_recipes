package com.recipes.recipe_api.model


data class Product(
    val id: Long,
    val name: String,
    val priceInCents: Int
)


data class Recipe(
    val id: Long,
    val name: String,
    val products: List<Product>
)


data class Cart(
    val id: Long,
    val totalInCents: Int,
    val items: List<CartItem>
)


data class CartItem(
    val id: Long,
    val productId: Long
)

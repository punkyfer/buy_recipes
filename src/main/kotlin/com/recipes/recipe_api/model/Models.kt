package com.recipes.recipe_api.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*


@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    val priceInCents: Int
)

@Entity
@Table(name = "recipes")
data class Recipe(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "recipe_products",
        joinColumns = [JoinColumn(name = "recipe_id")],
        inverseJoinColumns = [JoinColumn(name = "product_id")]
    )
    val products: Set<Product> = emptySet()
)

@Entity
@Table(name = "cart_recipes")
data class CartRecipe(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    val recipe: Recipe,

    var quantity: Int = 1,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    var cart: Cart? = null
)

@Entity
@Table(name = "cart_items")
data class CartItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: Product,

    var quantity: Int = 1,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    var cart: Cart? = null
)

@Entity
@Table(name = "carts")
data class Cart(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var totalInCents: Int = 0,

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<CartItem> = mutableListOf(),

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    val recipes: MutableList<CartRecipe> = mutableListOf()
) {
    fun recalculateTotal() {
        this.totalInCents = items.sumOf { it.product.priceInCents * it.quantity }
    }

    fun addItem(item: CartItem) {
        items.add(item)
        item.cart = this
    }

    fun addRecipe(cartRecipe: CartRecipe) {
        recipes.add(cartRecipe)
        cartRecipe.cart = this
    }
}

package com.recipes.recipe_api.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.recipes.recipe_api.exception.RecipeNotInCartException
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

    @Column(name = "recipe_id", insertable = false, updatable = false)
    val recipeId: Long = 0,

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

    @Column(name = "product_id", insertable = false, updatable = false)
    val productId: Long = 0,

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

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @MapKey(name = "productId")
    @JsonIgnore
    val items: MutableMap<Long, CartItem> = mutableMapOf(),

    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @MapKey(name = "recipeId")
    @JsonIgnore
    val recipes: MutableMap<Long, CartRecipe> = mutableMapOf()
) {
    val recipeItems: Collection<CartRecipe>
        @get:JsonProperty("recipes")
        get() = recipes.values

    val productItems: Collection<CartItem>
        @get:JsonProperty("items")
        get() = items.values

    private fun recalculateTotal() {
        this.totalInCents = items.values.sumOf { it.product.priceInCents * it.quantity }
    }

    private fun associateItem(item: CartItem) {
        items[item.productId] = item
        item.cart = this
    }

    private fun associateRecipe(cartRecipe: CartRecipe) {
        recipes[cartRecipe.recipeId] = cartRecipe
        cartRecipe.cart = this
    }

    fun addRecipe(recipe: Recipe) {
        val existingCartRecipe = this.recipes[recipe.id]
        if (existingCartRecipe != null) {
            existingCartRecipe.quantity++
        } else {
            this.associateRecipe(CartRecipe(recipe = recipe, recipeId = recipe.id, quantity = 1))
        }

        recipe.products.forEach { product ->
            val existingItem = this.items[product.id]
            if (existingItem != null) {
                existingItem.quantity++
            } else {
                this.associateItem(CartItem(product = product, productId = product.id, quantity = 1))
            }
        }
        recalculateTotal()
    }

    fun removeRecipe(recipeId: Long) {
        val cartRecipe = this.recipes[recipeId]
            ?: throw RecipeNotInCartException("Recipe with id $recipeId not in cart.")

        val recipeToRemove = cartRecipe.recipe

        recipeToRemove.products.forEach { product ->
            val itemToRemove = this.items[product.id]
            itemToRemove?.let {
                it.quantity--
                if (it.quantity <= 0) {
                    this.items.remove(product.id)
                }
            }
        }

        cartRecipe.quantity--
        if (cartRecipe.quantity <= 0) {
            this.recipes.remove(recipeId)
        }
        recalculateTotal()
    }
}

package com.recipes.recipe_api.controller

import com.recipes.recipe_api.model.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping
class ApiController {

    // --- Mock Data ---
    private val mockProducts = listOf(
        Product(1, "Flour", 200),
        Product(2, "Sugar", 150),
        Product(3, "Eggs", 300)
    )

    private val mockRecipes = listOf(
        Recipe(101, "Simple Cake", mockProducts)
    )

    private val mockCart = Cart(
        id = 1,
        totalInCents = 650,
        items = listOf(
            CartItem(1, 1),
            CartItem(2, 2),
            CartItem(3, 3)
        )
    )

    // 1. GET /recipes -> Lists the recipes available
    @GetMapping("/recipes")
    fun getRecipes(): ResponseEntity<List<Recipe>> {
        println("GET /recipes called")
        return ResponseEntity.ok(mockRecipes)
    }

    // 2. GET /carts/:id -> Returns a Cart by its ID
    @GetMapping("/carts/{id}")
    fun getCartById(@PathVariable id: Long): ResponseEntity<Cart> {
        println("GET /carts/$id called")
        return ResponseEntity.ok(mockCart)
    }

    // 3. POST /carts/:id/add_recipe -> Adds a recipe to a cart
    @PostMapping("/carts/{cartId}/add_recipe")
    fun addRecipeToCart(
        @PathVariable cartId: Long,
        @RequestBody request: AddRecipeRequest
    ): ResponseEntity<Cart> {
        println("POST /carts/$cartId/add_recipe called with recipeId: ${request.recipeId}")
        return ResponseEntity.ok(mockCart)
    }


    // 4. DELETE /carts/:cartId/recipes/:recipeId -> Removes a recipe from a cart
    @DeleteMapping("/carts/{cartId}/recipes/{recipeId}")
    fun removeRecipeFromCart(@PathVariable cartId: Long, @PathVariable recipeId: Long): ResponseEntity<Void> {
        println("DELETE /carts/$cartId/recipes/$recipeId called")
        return ResponseEntity.noContent().build()
    }
}
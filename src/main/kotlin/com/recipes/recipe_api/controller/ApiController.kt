package com.recipes.recipe_api.controller

import com.recipes.recipe_api.model.*
import com.recipes.recipe_api.service.CartService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
class ApiController(private val cartService: CartService) {

    @Operation(summary = "List all available recipes")
    @GetMapping("/recipes")
    fun getRecipes(): ResponseEntity<List<Recipe>> =
        ResponseEntity.ok(cartService.getAllRecipes())

    @Operation(summary = "Get a specific cart by its ID")
    @ApiResponse(responseCode = "200", description = "Cart found")
    @ApiResponse(responseCode = "404", description = "Cart not found", content = [Content(schema = Schema(implementation = Unit::class))])
    @GetMapping("/carts/{id}")
    fun getCartById(@PathVariable id: Long): ResponseEntity<Cart> =
        ResponseEntity.ok(cartService.findCartById(id))

    @Operation(summary = "Add a recipe to a cart")
    @ApiResponse(responseCode = "200", description = "Recipe added and cart returned")
    @ApiResponse(responseCode = "404", description = "Cart or Recipe not found", content = [Content(schema = Schema(implementation = Unit::class))])
    @PostMapping("/carts/{cartId}/add_recipe")
    fun addRecipeToCart(
        @PathVariable cartId: Long,
        @Valid @RequestBody request: AddRecipeRequest
    ): ResponseEntity<Cart> {
        val updatedCart = cartService.addRecipeToCart(cartId, request.recipeId)
        return ResponseEntity.ok(updatedCart)
    }

    @Operation(summary = "Remove a recipe from a cart")
    @ApiResponse(responseCode = "200", description = "Recipe removed and cart returned")
    @ApiResponse(responseCode = "404", description = "Cart not found or Recipe not in cart", content = [Content(schema = Schema(implementation = Unit::class))])
    @DeleteMapping("/carts/{cartId}/recipes/{recipeId}")
    fun removeRecipeFromCart(
        @PathVariable cartId: Long,
        @PathVariable recipeId: Long
    ): ResponseEntity<Cart> {
        val updatedCart = cartService.removeRecipeFromCart(cartId, recipeId)
        return ResponseEntity.ok(updatedCart)
    }
}
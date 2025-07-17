package com.recipes.recipe_api.service

import com.recipes.recipe_api.exception.RecipeNotInCartException
import com.recipes.recipe_api.model.Cart
import com.recipes.recipe_api.model.CartItem
import com.recipes.recipe_api.model.CartRecipe
import com.recipes.recipe_api.repository.CartRepository
import com.recipes.recipe_api.repository.RecipeRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val recipeRepository: RecipeRepository
) {

    fun getAllRecipes() = recipeRepository.findAll()

    fun findCartById(id: Long): Cart = cartRepository.findById(id)
        .orElseThrow { EntityNotFoundException("Cart with id $id not found") }

    @Transactional
    fun addRecipeToCart(cartId: Long, recipeId: Long): Cart {
        val cart = findCartById(cartId)
        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { EntityNotFoundException("Recipe with id $recipeId not found") }

        val existingCartRecipe = cart.recipes.find { it.recipe.id == recipeId }
        if (existingCartRecipe != null) {
            existingCartRecipe.quantity++
        } else {
            cart.addRecipe(CartRecipe(recipe = recipe, quantity = 1))
        }

        recipe.products.forEach { product ->
            val existingItem = cart.items.find { it.product.id == product.id }
            if (existingItem != null) {
                existingItem.quantity++
            } else {
                cart.addItem(CartItem(product = product, quantity = 1))
            }
        }

        cart.recalculateTotal()
        return cartRepository.save(cart)
    }

    @Transactional
    fun removeRecipeFromCart(cartId: Long, recipeId: Long): Cart {
        val cart = findCartById(cartId)

        val cartRecipe = cart.recipes.find { it.recipe.id == recipeId }
            ?: throw RecipeNotInCartException("Recipe with id $recipeId not in cart.")

        val recipeToRemove = cartRecipe.recipe

        cartRecipe.quantity--
        if (cartRecipe.quantity <= 0) {
            cart.recipes.remove(cartRecipe)
        }

        recipeToRemove.products.forEach { product ->
            val itemToRemove = cart.items.find { it.product.id == product.id }
            itemToRemove?.let { it.quantity-- }
        }

        cart.items.removeIf { it.quantity <= 0 }

        cart.recalculateTotal()
        return cartRepository.save(cart)
    }
}
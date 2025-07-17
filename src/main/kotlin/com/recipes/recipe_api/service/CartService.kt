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

        cart.addRecipe(recipe)
        return cartRepository.save(cart)
    }

    @Transactional
    fun removeRecipeFromCart(cartId: Long, recipeId: Long): Cart {
        val cart = findCartById(cartId)

        cart.removeRecipe(recipeId)
        return cartRepository.save(cart)
    }
}
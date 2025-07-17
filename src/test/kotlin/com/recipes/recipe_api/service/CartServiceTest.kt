package com.recipes.recipe_api.service

import com.recipes.recipe_api.model.Cart
import com.recipes.recipe_api.model.Recipe
import com.recipes.recipe_api.repository.CartRepository
import com.recipes.recipe_api.repository.RecipeRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.*

@ExtendWith(MockitoExtension::class)
class CartServiceTest {

    @Mock
    private lateinit var cartRepository: CartRepository

    @Mock
    private lateinit var recipeRepository: RecipeRepository

    @InjectMocks
    private lateinit var cartService: CartService

    @Test
    fun `addRecipeToCart should fetch cart and recipe, call cart's addRecipe, and save`() {
        val cartId = 1L
        val recipeId = 101L
        val mockCart = spy(Cart(id = cartId))
        val mockRecipe = Recipe(id = recipeId, name = "Cake", products = emptySet())

        whenever(cartRepository.findById(cartId)).thenReturn(Optional.of(mockCart))
        whenever(recipeRepository.findById(recipeId)).thenReturn(Optional.of(mockRecipe))
        whenever(cartRepository.save(any<Cart>())).thenReturn(mockCart)

        cartService.addRecipeToCart(cartId, recipeId)

        verify(cartRepository).findById(cartId)
        verify(recipeRepository).findById(recipeId)
        verify(mockCart).addRecipe(mockRecipe)
        verify(cartRepository).save(mockCart)
    }

    @Test
    fun `addRecipeToCart should throw EntityNotFoundException if cart does not exist`() {
        whenever(cartRepository.findById(any())).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
            cartService.addRecipeToCart(1L, 101L)
        }
    }

    @Test
    fun `removeRecipeFromCart should fetch cart, call cart's removeRecipe, and save`() {
        val cartId = 1L
        val recipeId = 101L
        val mockCart = spy(Cart(id = cartId))
        doNothing().whenever(mockCart).removeRecipe(any())

        whenever(cartRepository.findById(cartId)).thenReturn(Optional.of(mockCart))
        whenever(cartRepository.save(any<Cart>())).thenReturn(mockCart)

        cartService.removeRecipeFromCart(cartId, recipeId)

        verify(cartRepository).findById(cartId)
        verify(mockCart).removeRecipe(recipeId)
        verify(cartRepository).save(mockCart)
    }
}

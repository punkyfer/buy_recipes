package com.recipes.recipe_api.service

import com.recipes.recipe_api.exception.RecipeNotInCartException
import com.recipes.recipe_api.model.*
import com.recipes.recipe_api.repository.CartRepository
import com.recipes.recipe_api.repository.RecipeRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CartServiceTest {

    @Mock private lateinit var cartRepository: CartRepository
    @Mock private lateinit var recipeRepository: RecipeRepository
    @InjectMocks private lateinit var cartService: CartService

    private val flour = Product(1L, "Flour", 200)
    private val cakeRecipe = Recipe(101L, "Cake", setOf(flour))

    @Test
    fun `addRecipeToCart should work correctly`() {
        val cart = Cart(id = 1L)
        whenever(cartRepository.findById(1L)).thenReturn(Optional.of(cart))
        whenever(recipeRepository.findById(101L)).thenReturn(Optional.of(cakeRecipe))
        whenever(cartRepository.save(any<Cart>())).thenAnswer { it.getArgument(0) }

        val updatedCart = cartService.addRecipeToCart(1L, 101L)

        assertEquals(1, updatedCart.recipes.size)
        assertEquals(1, updatedCart.recipes.first().quantity)
    }

    @Test
    fun `removeRecipeFromCart should throw exception if recipe is not in cart`() {
        val emptyCart = Cart(id = 1L)
        whenever(cartRepository.findById(1L)).thenReturn(Optional.of(emptyCart))

        assertThrows(RecipeNotInCartException::class.java) {
            cartService.removeRecipeFromCart(1L, 101L)
        }
    }

}
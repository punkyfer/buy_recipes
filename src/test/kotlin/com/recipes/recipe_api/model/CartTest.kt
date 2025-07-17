package com.recipes.recipe_api.model

import com.recipes.recipe_api.exception.RecipeNotInCartException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CartTest {

    private lateinit var cart: Cart
    private val flour = Product(1L, "Flour", 200)
    private val sugar = Product(2L, "Sugar", 150)
    private val eggs = Product(3L, "Eggs", 300)

    private val cakeRecipe = Recipe(101L, "Cake", setOf(flour, sugar, eggs))
    private val pancakeRecipe = Recipe(102L, "Pancakes", setOf(flour, eggs))

    @BeforeEach
    fun setUp() {
        cart = Cart()
    }

    @Test
    fun `addRecipe should add a new recipe and its products to an empty cart`() {
        cart.addRecipe(cakeRecipe)

        assertEquals(1, cart.recipes.size)
        assertEquals(1, cart.recipes[101L]?.quantity)
        assertEquals(3, cart.items.size)
        assertEquals(1, cart.items[1L]?.quantity)
        assertEquals(650, cart.totalInCents)
    }

    @Test
    fun `addRecipe should increment quantity if the same recipe is added again`() {
        cart.addRecipe(cakeRecipe)

        cart.addRecipe(cakeRecipe)

        assertEquals(1, cart.recipes.size)
        assertEquals(2, cart.recipes[101L]?.quantity)
        assertEquals(3, cart.items.size)
        assertEquals(2, cart.items[1L]?.quantity)
        assertEquals(2, cart.items[2L]?.quantity)
        assertEquals(2, cart.items[3L]?.quantity)
        assertEquals(1300, cart.totalInCents)
    }

    @Test
    fun `addRecipe should handle overlapping products correctly`() {
        cart.addRecipe(cakeRecipe)

        cart.addRecipe(pancakeRecipe)

        assertEquals(2, cart.recipes.size)
        assertEquals(3, cart.items.size)
        assertEquals(2, cart.items[1L]?.quantity)
        assertEquals(1, cart.items[2L]?.quantity)
        assertEquals(2, cart.items[3L]?.quantity)
        assertEquals(1150, cart.totalInCents)
    }

    @Test
    fun `removeRecipe should decrement quantities correctly`() {
        cart.addRecipe(cakeRecipe)
        cart.addRecipe(cakeRecipe)

        cart.removeRecipe(101L)

        assertEquals(1, cart.recipes.size)
        assertEquals(1, cart.recipes[101L]?.quantity)
        assertEquals(1, cart.items[1L]?.quantity)
        assertEquals(650, cart.totalInCents)
    }

    @Test
    fun `removeRecipe should remove recipe and products when quantity reaches zero`() {
        cart.addRecipe(cakeRecipe)


        cart.removeRecipe(101L)

        assertTrue(cart.recipes.isEmpty())
        assertTrue(cart.items.isEmpty())
        assertEquals(0, cart.totalInCents)
    }

    @Test
    fun `removeRecipe should throw exception if recipe is not in the cart`() {
        assertThrows<RecipeNotInCartException> {
            cart.removeRecipe(999L)
        }
    }
}
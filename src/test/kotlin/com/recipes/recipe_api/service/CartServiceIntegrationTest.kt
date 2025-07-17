package com.recipes.recipe_api.service


import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase

@SpringBootTest
@Testcontainers
@Sql("/integration-test-data.sql")
class CartServiceIntegrationTest {

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer("postgres:15-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("spring.flyway.enabled") { "false" }
        }
    }

    @Autowired
    private lateinit var cartService: CartService

    @Test
    fun `should add a recipe to an empty cart correctly`() {
        val updatedCart = cartService.addRecipeToCart(1, 101)

        assertEquals(3, updatedCart.items.size)
        assertEquals(1, updatedCart.recipes.size)

        assertNotNull(updatedCart.recipes[101L])
        assertEquals(1, updatedCart.recipes[101L]?.quantity)
        assertEquals(101L, updatedCart.recipes[101L]?.recipe?.id)

        assertEquals(650, updatedCart.totalInCents)
    }

    @Test
    fun `should correctly increment quantities when adding recipes with overlapping ingredients`() {
        cartService.addRecipeToCart(1, 101) // Cake: Flour, Sugar, Eggs
        val finalCart = cartService.addRecipeToCart(1, 102) // Pancakes: Flour, Eggs

        // Assert cart contents
        assertEquals(2, finalCart.recipes.size)
        assertEquals(3, finalCart.items.size)

        // Assert recipe quantities (both should be 1)
        assertEquals(1, finalCart.recipes[101L]?.quantity)
        assertEquals(1, finalCart.recipes[102L]?.quantity)

        // Assert item quantities (Flour and Eggs are shared)
        assertEquals(2, finalCart.items[1L]?.quantity) // Flour
        assertEquals(1, finalCart.items[2L]?.quantity) // Sugar
        assertEquals(2, finalCart.items[3L]?.quantity) // Eggs

        // Assert total price: (2*200 + 1*150 + 2*300) = 400 + 150 + 600 = 1150
        assertEquals(1150, finalCart.totalInCents)
    }

    @Test
    fun `should correctly decrement quantities and remove items when a recipe is removed`() {
        cartService.addRecipeToCart(1, 101) // Cake: Flour, Sugar, Eggs
        cartService.addRecipeToCart(1, 102) // Pancakes: Flour, Eggs

        val updatedCart = cartService.removeRecipeFromCart(1, 101) // Remove Cake

        // Assert cart contents
        assertEquals(1, updatedCart.recipes.size)
        assertEquals(2, updatedCart.items.size) // Sugar (product 2) should be gone

        // Assert remaining recipe is Pancakes
        assertNull(updatedCart.recipes[101L])
        assertNotNull(updatedCart.recipes[102L])

        // Assert item quantities for Pancakes
        assertNull(updatedCart.items[2L]) // Sugar is gone
        assertEquals(1, updatedCart.items[1L]?.quantity) // Flour
        assertEquals(1, updatedCart.items[3L]?.quantity) // Eggs

        // Assert total price for Pancakes: (1*200 + 1*300) = 500
        assertEquals(500, updatedCart.totalInCents)
    }

    @Test
    fun `should correctly increment recipe and product quantities when adding the same recipe twice`() {
        cartService.addRecipeToCart(1, 101)
        val finalCart = cartService.addRecipeToCart(1, 101)

        assertEquals(1, finalCart.recipes.size)
        assertEquals(3, finalCart.items.size)

        // Assert recipe quantity has doubled
        assertEquals(2, finalCart.recipes[101L]?.quantity)

        // Assert all item quantities have doubled
        assertEquals(2, finalCart.items[1L]?.quantity) // Flour
        assertEquals(2, finalCart.items[2L]?.quantity) // Sugar
        assertEquals(2, finalCart.items[3L]?.quantity) // Eggs

        // Assert total price has doubled: 650 * 2 = 1300
        assertEquals(1300, finalCart.totalInCents)
    }

    @Test
    fun `should decrement then remove recipe on subsequent removals`() {
        // Arrange: Add the same recipe twice
        cartService.addRecipeToCart(1, 101)
        cartService.addRecipeToCart(1, 101)

        // Act 1: Remove the recipe once
        val cartAfterFirstRemoval = cartService.removeRecipeFromCart(1, 101)

        // Assert 1: Quantity should be 1, price should be halved
        assertEquals(1, cartAfterFirstRemoval.recipes.size)
        assertEquals(1, cartAfterFirstRemoval.recipes[101L]?.quantity)
        assertEquals(650, cartAfterFirstRemoval.totalInCents)
        assertEquals(1, cartAfterFirstRemoval.items[1L]?.quantity)
        assertEquals(1, cartAfterFirstRemoval.items[2L]?.quantity)
        assertEquals(1, cartAfterFirstRemoval.items[3L]?.quantity)

        // Act 2: Remove the recipe again
        val cartAfterSecondRemoval = cartService.removeRecipeFromCart(1, 101)

        // Assert 2: Cart should be empty
        assertTrue(cartAfterSecondRemoval.recipes.isEmpty())
        assertTrue(cartAfterSecondRemoval.items.isEmpty())
        assertEquals(0, cartAfterSecondRemoval.totalInCents)
    }
}
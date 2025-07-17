package com.recipes.recipe_api.service


import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.springframework.test.context.jdbc.Sql

@SpringBootTest
@Testcontainers
@Sql("/schema.sql")
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
        }
    }

    @Autowired
    private lateinit var cartService: CartService

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `should add a recipe to an empty cart correctly`() {
        val updatedCart = cartService.addRecipeToCart(1, 101)

        assertEquals(3, updatedCart.items.size)
        assertEquals(1, updatedCart.recipes.size)
        assertEquals(101L, updatedCart.recipes.first().recipe.id)
        assertEquals(650, updatedCart.totalInCents)
    }

    @Test
    fun `should correctly increment quantities when adding recipes with overlapping ingredients`() {
        cartService.addRecipeToCart(1, 101)
        val finalCart = cartService.addRecipeToCart(1, 102)

        assertEquals(3, finalCart.items.size)
        assertEquals(2, finalCart.recipes.size)

        assertEquals(2, finalCart.items.find { it.product.id == 1L }?.quantity)
        assertEquals(1, finalCart.items.find { it.product.id == 2L }?.quantity)
        assertEquals(2, finalCart.items.find { it.product.id == 3L }?.quantity)

        assertEquals(1150, finalCart.totalInCents)
    }

    @Test
    fun `should correctly decrement quantities and remove items when a recipe is removed`() {
        cartService.addRecipeToCart(1, 101)
        cartService.addRecipeToCart(1, 102)

        val updatedCart = cartService.removeRecipeFromCart(1, 101)

        assertEquals(1, updatedCart.recipes.size)
        assertEquals(102L, updatedCart.recipes.first().recipe.id)

        assertNull(updatedCart.items.find { it.product.id == 2L })

        assertEquals(1, updatedCart.items.find { it.product.id == 1L }?.quantity)
        assertEquals(1, updatedCart.items.find { it.product.id == 3L }?.quantity)
        assertEquals(2, updatedCart.items.size)
        assertEquals(500, updatedCart.totalInCents)
    }

    @Test
    fun `should correctly increment recipe and product quantities when adding the same recipe twice`() {
        cartService.addRecipeToCart(1, 101)
        val finalCart = cartService.addRecipeToCart(1, 101)

        assertEquals(1, finalCart.recipes.size)
        assertEquals(2, finalCart.recipes.first().quantity)

        assertEquals(2, finalCart.items.find { it.product.id == 1L }?.quantity)
        assertEquals(2, finalCart.items.find { it.product.id == 2L }?.quantity)
        assertEquals(2, finalCart.items.find { it.product.id == 3L }?.quantity)

        assertEquals(1300, finalCart.totalInCents)
    }

    @Test
    fun `should decrement then remove recipe on subsequent removals`() {
        cartService.addRecipeToCart(1, 101)
        cartService.addRecipeToCart(1, 101)

        val cartAfterFirstRemoval = cartService.removeRecipeFromCart(1, 101)

        assertEquals(1, cartAfterFirstRemoval.recipes.size)
        assertEquals(1, cartAfterFirstRemoval.recipes.first().quantity)
        assertEquals(650, cartAfterFirstRemoval.totalInCents)

        val cartAfterSecondRemoval = cartService.removeRecipeFromCart(1, 101)

        assertEquals(0, cartAfterSecondRemoval.recipes.size)
        assertEquals(0, cartAfterSecondRemoval.items.size)
        assertEquals(0, cartAfterSecondRemoval.totalInCents)
    }
}
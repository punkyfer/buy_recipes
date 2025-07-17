package com.recipes.recipe_api.repository

import com.recipes.recipe_api.model.Cart
import com.recipes.recipe_api.model.Recipe
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CartRepository : JpaRepository<Cart, Long>

@Repository
interface RecipeRepository : JpaRepository<Recipe, Long>
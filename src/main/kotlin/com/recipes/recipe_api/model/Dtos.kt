package com.recipes.recipe_api.model

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class AddRecipeRequest(
    @field:NotNull
    @field:Positive
    val recipeId: Long
)

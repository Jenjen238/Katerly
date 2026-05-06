package com.katerly.catering.repository;

import com.katerly.catering.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByUserUserId(Long userId);
    Optional<Recipe> findByRecipeIdAndUserUserId(Long recipeId, Long userId);
    List<Recipe> findByUserUserIdAndNamaResepContainingIgnoreCase(Long userId, String namaResep);
    long countByUserUserId(Long userId);
}
package com.katerly.catering.repository;

import com.katerly.catering.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    List<Ingredient> findByUserUserId(Long userId);
    Optional<Ingredient> findByIngredientIdAndUserUserId(Long ingredientId, Long userId);
    boolean existsByNamaIgnoreCaseAndUserUserId(String nama, Long userId);
    List<Ingredient> findByUserUserIdAndNamaContainingIgnoreCase(Long userId, String nama);
}
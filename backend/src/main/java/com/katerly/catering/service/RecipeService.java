package com.katerly.catering.service;

import com.katerly.catering.dto.request.RecipeRequest;
import com.katerly.catering.dto.response.RecipeResponse;
import com.katerly.catering.entity.*;
import com.katerly.catering.exception.BadRequestException;
import com.katerly.catering.exception.ResourceNotFoundException;
import com.katerly.catering.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;

    private static final int MAX_RECIPE_FREE = 4;

    // ─── CREATE ───────────────────────────────────────────────────────────────────
    @Transactional
    public RecipeResponse create(Long userId, RecipeRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        // Cek batas resep untuk akun gratis
        if (!user.isPremium() && recipeRepository.countByUserUserId(userId) >= MAX_RECIPE_FREE) {
            throw new BadRequestException(
                    "Akun gratis hanya bisa membuat " + MAX_RECIPE_FREE + " resep. Upgrade ke Premium untuk resep tanpa batas.");
        }

        Recipe recipe = Recipe.builder()
                .user(user)
                .namaResep(req.getNamaResep())
                .jumlahPorsi(req.getJumlahPorsi())
                .margin(req.getMargin())
                .hppManual(req.getHppManual())
                .recipeIngredients(new ArrayList<>())
                .build();

        // Tambah bahan jika ada
        if (req.getIngredients() != null && !req.getIngredients().isEmpty()) {
            List<RecipeIngredient> ingredients = buildIngredients(req.getIngredients(), recipe, userId);
            recipe.getRecipeIngredients().addAll(ingredients);
        }

        // Kalkulasi HPP
        calculateHpp(recipe);

        return toResponse(recipeRepository.save(recipe));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────────
    @Transactional
    public RecipeResponse update(Long userId, Long recipeId, RecipeRequest req) {
        Recipe recipe = recipeRepository.findByRecipeIdAndUserUserId(recipeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resep tidak ditemukan"));

        recipe.setNamaResep(req.getNamaResep());
        recipe.setJumlahPorsi(req.getJumlahPorsi());
        recipe.setMargin(req.getMargin());
        recipe.setHppManual(req.getHppManual());
        recipe.getRecipeIngredients().clear();

        if (req.getIngredients() != null && !req.getIngredients().isEmpty()) {
            List<RecipeIngredient> ingredients = buildIngredients(req.getIngredients(), recipe, userId);
            recipe.getRecipeIngredients().addAll(ingredients);
        }

        calculateHpp(recipe);

        return toResponse(recipeRepository.save(recipe));
    }

    // ─── GET ALL ──────────────────────────────────────────────────────────────────
    public List<RecipeResponse> getAll(Long userId) {
        return recipeRepository.findByUserUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── GET BY ID ────────────────────────────────────────────────────────────────
    public RecipeResponse getById(Long userId, Long recipeId) {
        Recipe recipe = recipeRepository.findByRecipeIdAndUserUserId(recipeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resep tidak ditemukan"));
        return toResponse(recipe);
    }

    // ─── SEARCH ───────────────────────────────────────────────────────────────────
    public List<RecipeResponse> search(Long userId, String keyword) {
        return recipeRepository.findByUserUserIdAndNamaResepContainingIgnoreCase(userId, keyword)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────────
    @Transactional
    public void delete(Long userId, Long recipeId) {
        Recipe recipe = recipeRepository.findByRecipeIdAndUserUserId(recipeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resep tidak ditemukan"));
        recipeRepository.delete(recipe);
    }

    // ─── KALKULASI HPP ────────────────────────────────────────────────────────────
    private void calculateHpp(Recipe recipe) {
        // Jika HPP manual diisi, pakai itu
        if (recipe.getHppManual() != null && recipe.getHppManual().compareTo(BigDecimal.ZERO) > 0) {
            recipe.setHppFinal(recipe.getHppManual());
        } else if (!recipe.getRecipeIngredients().isEmpty()) {
            // Auto kalkulasi dari bahan
            BigDecimal totalHpp = recipe.getRecipeIngredients().stream()
                    .map(ri -> ri.getIngredient().getHargaPerSatuan()
                            .multiply(ri.getQuantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            recipe.setHppFinal(totalHpp);
        } else {
            recipe.setHppFinal(BigDecimal.ZERO);
        }

        // Hitung harga jual = hppFinal / (1 - margin/100)
        if (recipe.getHppFinal().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal marginFactor = BigDecimal.ONE
                    .subtract(recipe.getMargin().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            if (marginFactor.compareTo(BigDecimal.ZERO) > 0) {
                recipe.setHargaJual(recipe.getHppFinal()
                        .divide(marginFactor, 2, RoundingMode.CEILING));
            }
        }
    }

    // ─── BUILD INGREDIENTS ────────────────────────────────────────────────────────
    private List<RecipeIngredient> buildIngredients(
            List<RecipeRequest.RecipeIngredientItem> items, Recipe recipe, Long userId) {
        List<RecipeIngredient> result = new ArrayList<>();
        for (RecipeRequest.RecipeIngredientItem item : items) {
            Ingredient ingredient = ingredientRepository
                    .findByIngredientIdAndUserUserId(item.getIngredientId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Bahan ID " + item.getIngredientId() + " tidak ditemukan"));
            result.add(RecipeIngredient.builder()
                    .recipe(recipe)
                    .ingredient(ingredient)
                    .quantity(item.getQuantity())
                    .build());
        }
        return result;
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────────
    public RecipeResponse toResponse(Recipe r) {
        List<RecipeResponse.RecipeIngredientResponse> ingredients = r.getRecipeIngredients()
                .stream()
                .map(ri -> RecipeResponse.RecipeIngredientResponse.builder()
                        .ingredientId(ri.getIngredient().getIngredientId())
                        .namaIngredient(ri.getIngredient().getNama())
                        .satuan(ri.getIngredient().getSatuan())
                        .quantity(ri.getQuantity())
                        .hargaPerSatuan(ri.getIngredient().getHargaPerSatuan())
                        .subtotal(ri.getIngredient().getHargaPerSatuan()
                                .multiply(ri.getQuantity())
                                .setScale(2, RoundingMode.HALF_UP))
                        .build())
                .collect(Collectors.toList());

        return RecipeResponse.builder()
                .recipeId(r.getRecipeId())
                .namaResep(r.getNamaResep())
                .jumlahPorsi(r.getJumlahPorsi())
                .margin(r.getMargin())
                .hppManual(r.getHppManual())
                .hppFinal(r.getHppFinal())
                .hargaJual(r.getHargaJual())
                .ingredients(ingredients)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
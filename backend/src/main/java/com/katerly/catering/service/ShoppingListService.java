package com.katerly.catering.service;

import com.katerly.catering.dto.request.ShoppingListRequest;
import com.katerly.catering.dto.response.ShoppingListResponse;
import com.katerly.catering.entity.*;
import com.katerly.catering.exception.BadRequestException;
import com.katerly.catering.exception.ResourceNotFoundException;
import com.katerly.catering.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;

    // ─── GENERATE SHOPPING LIST ───────────────────────────────────────────────────
    @Transactional
    public ShoppingListResponse generate(Long userId, ShoppingListRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        // Validasi duplikat resep
        List<Long> uniqueIds = req.getRecipeIds().stream().distinct().collect(Collectors.toList());
        if (uniqueIds.size() != req.getRecipeIds().size()) {
            throw new BadRequestException("Resep tidak boleh duplikat");
        }

        if (req.getRecipeIds().size() > 4) {
            throw new BadRequestException("Maksimal 4 resep per daftar belanja");
        }

        // Buat shopping list baru
        ShoppingList shoppingList = ShoppingList.builder()
                .user(user)
                .shoppingListRecipes(new ArrayList<>())
                .shoppingListItems(new ArrayList<>())
                .build();

        // Akumulasi bahan dari semua resep
        Map<Long, BahanAcc> bahanMap = new HashMap<>();
        List<String> namaResep = new ArrayList<>();

        for (Long recipeId : req.getRecipeIds()) {
            Recipe recipe = recipeRepository.findByRecipeIdAndUserUserId(recipeId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Resep ID " + recipeId + " tidak ditemukan"));

            if (recipe.getRecipeIngredients().isEmpty()) {
                throw new BadRequestException("Resep '" + recipe.getNamaResep() + "' belum memiliki bahan. Tambahkan bahan terlebih dahulu.");
            }

            namaResep.add(recipe.getNamaResep());

            // Tambah relasi resep ke shopping list
            shoppingList.getShoppingListRecipes().add(
                    ShoppingListRecipe.builder()
                            .shoppingList(shoppingList)
                            .recipe(recipe)
                            .build());

            // Akumulasi bahan
            for (RecipeIngredient ri : recipe.getRecipeIngredients()) {
                Long ingId = ri.getIngredient().getIngredientId();
                BigDecimal totalQty = ri.getQuantity().multiply(BigDecimal.valueOf(recipe.getJumlahPorsi()));
                bahanMap.computeIfAbsent(ingId, k -> new BahanAcc(ri.getIngredient())).add(totalQty);
            }
        }

        // Build shopping list items
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (BahanAcc acc : bahanMap.values()) {
            BigDecimal totalHarga = acc.totalQty
                    .multiply(acc.ingredient.getHargaPerSatuan())
                    .setScale(2, RoundingMode.HALF_UP);

            ShoppingListItem item = ShoppingListItem.builder()
                    .shoppingList(shoppingList)
                    .ingredient(acc.ingredient)
                    .totalQuantity(acc.totalQty.setScale(3, RoundingMode.HALF_UP))
                    .satuan(acc.ingredient.getSatuan())
                    .totalHarga(totalHarga)
                    .build();

            shoppingList.getShoppingListItems().add(item);
            grandTotal = grandTotal.add(totalHarga);
        }

        ShoppingList saved = shoppingListRepository.save(shoppingList);
        return toResponse(saved, namaResep, grandTotal);
    }

    // ─── GET ALL ──────────────────────────────────────────────────────────────────
    public List<ShoppingListResponse> getAll(Long userId) {
        return shoppingListRepository.findByUserUserIdOrderByCreatedAtDesc(userId)
                .stream().map(sl -> {
                    List<String> namaResep = sl.getShoppingListRecipes().stream()
                            .map(r -> r.getRecipe().getNamaResep())
                            .collect(Collectors.toList());
                    BigDecimal total = sl.getShoppingListItems().stream()
                            .map(ShoppingListItem::getTotalHarga)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return toResponse(sl, namaResep, total);
                })
                .collect(Collectors.toList());
    }

    // ─── GET BY ID ────────────────────────────────────────────────────────────────
    public ShoppingListResponse getById(Long userId, Long shoppingListId) {
        ShoppingList sl = shoppingListRepository.findByShoppingListIdAndUserUserId(shoppingListId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Daftar belanja tidak ditemukan"));
        List<String> namaResep = sl.getShoppingListRecipes().stream()
                .map(r -> r.getRecipe().getNamaResep())
                .collect(Collectors.toList());
        BigDecimal total = sl.getShoppingListItems().stream()
                .map(ShoppingListItem::getTotalHarga)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return toResponse(sl, namaResep, total);
    }

    // ─── UPDATE IS BOUGHT ─────────────────────────────────────────────────────────
    @Transactional
    public ShoppingListResponse updateIsBought(Long userId, Long shoppingListId, Long itemId, boolean isBought) {
        ShoppingList sl = shoppingListRepository.findByShoppingListIdAndUserUserId(shoppingListId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Daftar belanja tidak ditemukan"));

        // Validasi bahwa item benar-benar milik shopping list ini
        ShoppingListItem item = sl.getShoppingListItems().stream()
                .filter(i -> i.getShoppingListItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item tidak ditemukan di daftar belanja ini"));

        item.setBought(isBought);
        shoppingListItemRepository.save(item);

        List<String> namaResep = sl.getShoppingListRecipes().stream()
                .map(r -> r.getRecipe().getNamaResep())
                .collect(Collectors.toList());
        BigDecimal total = sl.getShoppingListItems().stream()
                .map(ShoppingListItem::getTotalHarga)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return toResponse(sl, namaResep, total);
    }

    // ─── ADD ITEM (sync ke ingredient & recipe) ───────────────────────────────────
    @Transactional
    public ShoppingListResponse addItem(Long userId, Long shoppingListId, Long ingredientId, BigDecimal quantity) {
        ShoppingList sl = shoppingListRepository.findByShoppingListIdAndUserUserId(shoppingListId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Daftar belanja tidak ditemukan"));

        Ingredient ingredient = ingredientRepository.findByIngredientIdAndUserUserId(ingredientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bahan tidak ditemukan"));

        BigDecimal totalHarga = ingredient.getHargaPerSatuan()
                .multiply(quantity).setScale(2, RoundingMode.HALF_UP);

        ShoppingListItem newItem = ShoppingListItem.builder()
                .shoppingList(sl)
                .ingredient(ingredient)
                .totalQuantity(quantity)
                .satuan(ingredient.getSatuan())
                .totalHarga(totalHarga)
                .build();

        sl.getShoppingListItems().add(newItem);

        // Sync ke semua resep dalam shopping list ini
        for (ShoppingListRecipe slr : sl.getShoppingListRecipes()) {
            Recipe recipe = slr.getRecipe();
            boolean alreadyExists = recipe.getRecipeIngredients().stream()
                    .anyMatch(ri -> ri.getIngredient().getIngredientId().equals(ingredientId));

            if (!alreadyExists) {
                if (recipe.getJumlahPorsi() == null || recipe.getJumlahPorsi() == 0) {
                    throw new BadRequestException("Jumlah porsi resep '" + recipe.getNamaResep() + "' tidak valid");
                }
                BigDecimal qtyPerPorsi = quantity.divide(
                        BigDecimal.valueOf(recipe.getJumlahPorsi()), 3, RoundingMode.HALF_UP);
                recipe.getRecipeIngredients().add(RecipeIngredient.builder()
                        .recipe(recipe)
                        .ingredient(ingredient)
                        .quantity(qtyPerPorsi)
                        .build());
                recipeRepository.save(recipe);
            }
        }

        ShoppingList saved = shoppingListRepository.save(sl);
        List<String> namaResep = saved.getShoppingListRecipes().stream()
                .map(r -> r.getRecipe().getNamaResep()).collect(Collectors.toList());
        BigDecimal total = saved.getShoppingListItems().stream()
                .map(ShoppingListItem::getTotalHarga).reduce(BigDecimal.ZERO, BigDecimal::add);
        return toResponse(saved, namaResep, total);
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────────
    @Transactional
    public void delete(Long userId, Long shoppingListId) {
        ShoppingList sl = shoppingListRepository.findByShoppingListIdAndUserUserId(shoppingListId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Daftar belanja tidak ditemukan"));
        shoppingListRepository.delete(sl);
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────────
    private ShoppingListResponse toResponse(ShoppingList sl, List<String> namaResep, BigDecimal total) {
        List<ShoppingListResponse.ShoppingListItemResponse> items = sl.getShoppingListItems().stream()
                .map(i -> ShoppingListResponse.ShoppingListItemResponse.builder()
                        .shoppingListItemId(i.getShoppingListItemId())
                        .ingredientId(i.getIngredient().getIngredientId())
                        .namaIngredient(i.getIngredient().getNama())
                        .satuan(i.getSatuan())
                        .totalQuantity(i.getTotalQuantity())
                        .hargaPerSatuan(i.getIngredient().getHargaPerSatuan())
                        .totalHarga(i.getTotalHarga())
                        .isBought(i.isBought())
                        .build())
                .collect(Collectors.toList());

        return ShoppingListResponse.builder()
                .shoppingListId(sl.getShoppingListId())
                .namaResep(namaResep)
                .items(items)
                .totalHarga(total.setScale(2, RoundingMode.HALF_UP))
                .createdAt(sl.getCreatedAt())
                .build();
    }

    private static class BahanAcc {
        Ingredient ingredient;
        BigDecimal totalQty = BigDecimal.ZERO;
        BahanAcc(Ingredient i) { this.ingredient = i; }
        void add(BigDecimal qty) { totalQty = totalQty.add(qty); }
    }
}
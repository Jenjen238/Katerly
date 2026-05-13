package com.katerly.catering.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RecipeResponse {

    private Long recipeId;
    private String namaResep;
    private Integer jumlahPorsi;
    private BigDecimal margin;
    private BigDecimal hppManual;
    private BigDecimal hppFinal;
    private BigDecimal hargaJual;
    private List<RecipeIngredientResponse> ingredients;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class RecipeIngredientResponse {
        private Long ingredientId;
        private String namaIngredient;
        private String satuan;
        private BigDecimal quantity;
        private BigDecimal hargaPerSatuan;
        private BigDecimal subtotal;
    }
}
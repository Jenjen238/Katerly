package com.katerly.catering.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ShoppingListResponse {

    private Long shoppingListId;
    private List<String> namaResep;
    private List<ShoppingListItemResponse> items;
    private BigDecimal totalHarga;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class ShoppingListItemResponse {
        private Long shoppingListItemId;
        private Long ingredientId;
        private String namaIngredient;
        private String satuan;
        private BigDecimal totalQuantity;
        private BigDecimal hargaPerSatuan;
        private BigDecimal totalHarga;
        private boolean isBought;
    }
}
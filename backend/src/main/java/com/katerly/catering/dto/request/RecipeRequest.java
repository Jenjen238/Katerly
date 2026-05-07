package com.katerly.catering.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RecipeRequest {

    @NotBlank(message = "Nama resep wajib diisi")
    @Size(max = 100, message = "Nama resep maksimal 100 karakter")
    private String namaResep;

    @NotNull(message = "Jumlah porsi wajib diisi")
    @Min(value = 1, message = "Jumlah porsi minimal 1")
    private Integer jumlahPorsi;

    @NotNull(message = "Margin wajib diisi")
    @DecimalMin(value = "0.01", message = "Margin harus lebih dari 0")
    private BigDecimal margin;

    // Opsional, jika diisi maka HPP manual yang dipakai
    private BigDecimal hppManual;

    // Opsional, jika tidak diisi maka tidak bisa generate daftar belanja
    private List<RecipeIngredientItem> ingredients;

    @Data
    public static class RecipeIngredientItem {

        @NotNull(message = "ID bahan wajib diisi")
        private Long ingredientId;

        @NotNull(message = "Jumlah bahan wajib diisi")
        @DecimalMin(value = "0.001", message = "Jumlah minimal 0.001")
        private BigDecimal quantity;
    }
}
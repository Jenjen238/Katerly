package com.katerly.catering.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class IngredientResponse {

    private Long ingredientId;
    private String nama;
    private String satuan;
    private BigDecimal hargaPerSatuan;
    private BigDecimal hargaSebelumnya;

    // Tren perubahan harga: positif = naik, negatif = turun, 0 = tidak berubah
    private BigDecimal trendPersen;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
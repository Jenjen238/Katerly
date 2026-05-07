package com.katerly.catering.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class IngredientRequest {

    @NotBlank(message = "Nama bahan wajib diisi")
    @Size(max = 100, message = "Nama bahan maksimal 100 karakter")
    private String nama;

    @NotBlank(message = "Satuan wajib diisi")
    @Size(max = 20, message = "Satuan maksimal 20 karakter")
    private String satuan;

    @NotNull(message = "Harga per satuan wajib diisi")
    @DecimalMin(value = "0.01", message = "Harga harus lebih dari 0")
    private BigDecimal hargaPerSatuan;
}
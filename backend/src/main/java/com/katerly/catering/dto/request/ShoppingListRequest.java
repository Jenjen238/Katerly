package com.katerly.catering.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class ShoppingListRequest {

    @NotEmpty(message = "Minimal 1 resep harus dipilih")
    @Size(max = 4, message = "Maksimal 4 resep per daftar belanja")
    private List<Long> recipeIds;
}
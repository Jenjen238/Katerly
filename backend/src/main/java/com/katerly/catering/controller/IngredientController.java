package com.katerly.catering.controller;

import com.katerly.catering.dto.request.IngredientRequest;
import com.katerly.catering.dto.response.ApiResponse;
import com.katerly.catering.security.JwtUtil;
import com.katerly.catering.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@Tag(name = "Ingredients", description = "Kelola daftar bahan & harga")
public class IngredientController {

    private final IngredientService ingredientService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "Ambil semua bahan")
    public ResponseEntity<ApiResponse<?>> getAll(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                ingredientService.getAll(getUserId(request))));
    }

    @GetMapping("/search")
    @Operation(summary = "Search bahan berdasarkan nama")
    public ResponseEntity<ApiResponse<?>> search(
            HttpServletRequest request,
            @RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                ingredientService.search(getUserId(request), keyword)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ambil detail bahan")
    public ResponseEntity<ApiResponse<?>> getById(
            HttpServletRequest request,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                ingredientService.getById(getUserId(request), id)));
    }

    @PostMapping
    @Operation(summary = "Tambah bahan baru")
    public ResponseEntity<ApiResponse<?>> create(
            HttpServletRequest request,
            @Valid @RequestBody IngredientRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Bahan berhasil ditambahkan",
                ingredientService.create(getUserId(request), req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update bahan — harga sebelumnya otomatis tersimpan untuk tren")
    public ResponseEntity<ApiResponse<?>> update(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody IngredientRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Bahan berhasil diperbarui",
                ingredientService.update(getUserId(request), id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hapus bahan")
    public ResponseEntity<ApiResponse<?>> delete(
            HttpServletRequest request,
            @PathVariable Long id) {
        ingredientService.delete(getUserId(request), id);
        return ResponseEntity.ok(ApiResponse.success("Bahan berhasil dihapus"));
    }

    // ─── Helper ──────────────────────────────────────────────────────────────────
    private Long getUserId(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return jwtUtil.extractUserId(cookie.getValue());
                }
            }
        }
        return null;
    }
}
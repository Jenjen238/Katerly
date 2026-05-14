package com.katerly.catering.controller;

import com.katerly.catering.exception.BadRequestException;
import com.katerly.catering.dto.request.RecipeRequest;
import com.katerly.catering.dto.response.ApiResponse;
import com.katerly.catering.security.JwtUtil;
import com.katerly.catering.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@Tag(name = "Recipes", description = "Kelola resep & kalkulasi HPP")
public class RecipeController {

    private final RecipeService recipeService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "Ambil semua resep")
    public ResponseEntity<ApiResponse<?>> getAll(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                recipeService.getAll(getUserId(request))));
    }

    @GetMapping("/search")
    @Operation(summary = "Search resep berdasarkan nama")
    public ResponseEntity<ApiResponse<?>> search(
            HttpServletRequest request,
            @RequestParam String keyword) {
        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                recipeService.search(getUserId(request), keyword)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ambil detail resep")
    public ResponseEntity<ApiResponse<?>> getById(
            HttpServletRequest request,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                recipeService.getById(getUserId(request), id)));
    }

    @PostMapping
    @Operation(summary = "Buat resep baru — HPP auto kalkulasi dari bahan, kecuali HPP manual diisi")
    public ResponseEntity<ApiResponse<?>> create(
            HttpServletRequest request,
            @Valid @RequestBody RecipeRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Resep berhasil dibuat",
                recipeService.create(getUserId(request), req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update resep")
    public ResponseEntity<ApiResponse<?>> update(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Resep berhasil diperbarui",
                recipeService.update(getUserId(request), id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hapus resep")
    public ResponseEntity<ApiResponse<?>> delete(
            HttpServletRequest request,
            @PathVariable Long id) {
        recipeService.delete(getUserId(request), id);
        return ResponseEntity.ok(ApiResponse.success("Resep berhasil dihapus"));
    }

    // ─── Helper ──────────────────────────────────────────────────────────────────
    private Long getUserId(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        if (token == null) {
            throw new BadRequestException("Token tidak ditemukan, silakan login terlebih dahulu");
        }
        return jwtUtil.extractUserId(token);
    }
}
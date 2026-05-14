package com.katerly.catering.controller;

import com.katerly.catering.exception.BadRequestException;
import com.katerly.catering.dto.request.ShoppingListRequest;
import com.katerly.catering.dto.response.ApiResponse;
import com.katerly.catering.security.JwtUtil;
import com.katerly.catering.service.ShoppingListService;
import com.katerly.catering.service.ShoppingListPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/shopping-lists")
@RequiredArgsConstructor
@Tag(name = "Shopping List", description = "Generate & kelola daftar belanja")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final ShoppingListPdfService shoppingListPdfService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "Ambil semua daftar belanja")
    public ResponseEntity<ApiResponse<?>> getAll(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                shoppingListService.getAll(getUserId(request))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ambil detail daftar belanja")
    public ResponseEntity<ApiResponse<?>> getById(
            HttpServletRequest request,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                shoppingListService.getById(getUserId(request), id)));
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate daftar belanja dari resep (max 4 resep)")
    public ResponseEntity<ApiResponse<?>> generate(
            HttpServletRequest request,
            @Valid @RequestBody ShoppingListRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Daftar belanja berhasil digenerate",
                shoppingListService.generate(getUserId(request), req)));
    }

    @PatchMapping("/{id}/items/{itemId}/bought")
    @Operation(summary = "Update status sudah dibeli atau belum")
    public ResponseEntity<ApiResponse<?>> updateIsBought(
            HttpServletRequest request,
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestParam boolean isBought) {
        return ResponseEntity.ok(ApiResponse.success("Status diperbarui",
                shoppingListService.updateIsBought(getUserId(request), id, itemId, isBought)));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Tambah bahan ke daftar belanja — otomatis sync ke bahan & resep")
    public ResponseEntity<ApiResponse<?>> addItem(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam Long ingredientId,
            @RequestParam BigDecimal quantity) {
        return ResponseEntity.ok(ApiResponse.success("Bahan berhasil ditambahkan",
                shoppingListService.addItem(getUserId(request), id, ingredientId, quantity)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hapus daftar belanja")
    public ResponseEntity<ApiResponse<?>> delete(
            HttpServletRequest request,
            @PathVariable Long id) {
        shoppingListService.delete(getUserId(request), id);
        return ResponseEntity.ok(ApiResponse.success("Daftar belanja berhasil dihapus"));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Download PDF daftar belanja")
    public ResponseEntity<byte[]> downloadPdf(
            HttpServletRequest request,
            @PathVariable Long id) throws IOException {
        byte[] pdf = shoppingListPdfService.generatePdf(getUserId(request), id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("daftar-belanja-" + id + ".pdf").build());
        return ResponseEntity.ok().headers(headers).body(pdf);
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
package com.katerly.catering.controller;

import com.katerly.catering.dto.request.BusinessProfileRequest;
import com.katerly.catering.dto.response.ApiResponse;
import com.katerly.catering.security.JwtUtil;
import com.katerly.catering.service.BusinessProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/business-profile")
@RequiredArgsConstructor
@Tag(name = "Business Profile", description = "Kelola profil usaha catering")
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "Ambil profil bisnis")
    public ResponseEntity<ApiResponse<?>> getProfile(HttpServletRequest request) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(ApiResponse.success("Profil berhasil diambil",
                businessProfileService.getProfile(userId)));
    }

    @PostMapping
    @Operation(summary = "Buat atau update profil bisnis")
    public ResponseEntity<ApiResponse<?>> saveProfile(
            HttpServletRequest request,
            @Valid @RequestBody BusinessProfileRequest req) {
        Long userId = getUserId(request);
        return ResponseEntity.ok(ApiResponse.success("Profil berhasil disimpan",
                businessProfileService.saveProfile(userId, req)));
    }

    @PostMapping("/logo")
    @Operation(summary = "Upload logo usaha")
    public ResponseEntity<ApiResponse<?>> uploadLogo(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) throws IOException {
        Long userId = getUserId(request);
        return ResponseEntity.ok(ApiResponse.success("Logo berhasil diupload",
                businessProfileService.uploadLogo(userId, file)));
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
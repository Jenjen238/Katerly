package com.katerly.catering.controller;

import com.katerly.catering.exception.BadRequestException;
import com.katerly.catering.dto.response.ApiResponse;
import com.katerly.catering.security.JwtUtil;
import com.katerly.catering.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Statistik & insight usaha catering")
public class DashboardController {

    private final DashboardService dashboardService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "Ambil data dashboard — default bulan ini")
    public ResponseEntity<ApiResponse<?>> getDashboard(
            HttpServletRequest request,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        // Default ke bulan & tahun saat ini
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();

        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                dashboardService.getDashboard(getUserId(request), y, m)));
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
package com.katerly.catering.controller;

import com.katerly.catering.exception.BadRequestException;
import com.katerly.catering.dto.response.ApiResponse;
import com.katerly.catering.security.JwtUtil;
import com.katerly.catering.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Kelola langganan premium via Midtrans")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    @Operation(summary = "Buat transaksi pembayaran premium — returns snap token & payment URL")
    public ResponseEntity<ApiResponse<?>> createTransaction(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transaksi berhasil dibuat",
                subscriptionService.createTransaction(getUserId(request))));
    }

    @GetMapping("/active")
    @Operation(summary = "Cek status langganan premium aktif")
    public ResponseEntity<ApiResponse<?>> getActive(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                subscriptionService.getActiveSubscription(getUserId(request))));
    }

    @GetMapping("/history")
    @Operation(summary = "Riwayat transaksi langganan")
    public ResponseEntity<ApiResponse<?>> getHistory(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                subscriptionService.getHistory(getUserId(request))));
    }

    // Webhook dari Midtrans — tidak perlu auth
    @PostMapping("/webhook")
    @Operation(summary = "Webhook notifikasi pembayaran dari Midtrans")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        subscriptionService.handleNotification(payload);
        return ResponseEntity.ok("OK");
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
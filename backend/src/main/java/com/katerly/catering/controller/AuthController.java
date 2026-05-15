package com.katerly.catering.controller;

import com.katerly.catering.exception.BadRequestException;
import com.katerly.catering.dto.request.*;
import com.katerly.catering.dto.response.ApiResponse;
import com.katerly.catering.service.AuthService;
import com.katerly.catering.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.katerly.catering.service.GoogleAuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, Login, Logout, Forgot Password")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "Register akun baru")
    public ResponseEntity<ApiResponse<?>> register(
            @Valid @RequestBody RegisterRequest req,
            HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success("Registrasi berhasil",
                authService.register(req, response)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login dengan email dan password")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success("Login berhasil",
                authService.login(req, response)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout dan hapus cookie")
    public ResponseEntity<ApiResponse<?>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        Long userId = getUserIdFromCookie(request);
        if (userId != null) {
            authService.logout(userId, response);
        } else {
            // Tetap hapus cookie meskipun token invalid/tidak ada
            authService.logout(-1L, response);
        }
        return ResponseEntity.ok(ApiResponse.success("Logout berhasil"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token menggunakan refresh token dari cookie")
    public ResponseEntity<ApiResponse<?>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = getCookieValue(request, "refresh_token");
        if (refreshToken == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Refresh token tidak ditemukan"));
        }
        return ResponseEntity.ok(ApiResponse.success("Token diperbarui",
                authService.refreshToken(refreshToken, response)));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Kirim email reset password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return ResponseEntity.ok(ApiResponse.success("Email reset password telah dikirim"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password dengan token dari email")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(ApiResponse.success("Password berhasil direset, silakan login ulang"));
    }

    @Autowired
    private GoogleAuthService googleAuthService;

        @PostMapping("/google")
        @Operation(summary = "Login dengan Google token dari frontend")
        public ResponseEntity<ApiResponse<?>> loginWithGoogle(
                @Valid @RequestBody GoogleTokenRequest req,
                HttpServletResponse response) {
            return ResponseEntity.ok(ApiResponse.success("Login berhasil",
                    googleAuthService.loginWithGoogleToken(req, response)));
    }

    // ─── Helper ──────────────────────────────────────────────────────────────────
    private Long getUserIdFromCookie(HttpServletRequest request) {
        String accessToken = getCookieValue(request, "access_token");
        if (accessToken != null && jwtUtil.isTokenValid(accessToken)) {
            return jwtUtil.extractUserId(accessToken);
        }
        return null;
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}   
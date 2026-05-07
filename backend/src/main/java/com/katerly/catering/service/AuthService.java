package com.katerly.catering.service;

import com.katerly.catering.dto.request.*;
import com.katerly.catering.dto.response.AuthResponse;
import com.katerly.catering.entity.*;
import com.katerly.catering.exception.BadRequestException;
import com.katerly.catering.exception.ResourceNotFoundException;
import com.katerly.catering.repository.*;
import com.katerly.catering.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.reset-password.expiration}")
    private long resetPasswordExpiration;

    // ─── REGISTER ────────────────────────────────────────────────────────────────
    @Transactional
    public AuthResponse register(RegisterRequest req, HttpServletResponse response) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email sudah terdaftar");
        }

        User user = User.builder()
                .namaPemilik(req.getNamaPemilik())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();
        user = userRepository.save(user);

        return generateTokensAndBuildResponse(user, response);
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────────
    @Transactional
    public AuthResponse login(LoginRequest req, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Email atau password salah");
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        return generateTokensAndBuildResponse(user, response);
    }

    // ─── LOGOUT ──────────────────────────────────────────────────────────────────
    @Transactional
    public void logout(Long userId, HttpServletResponse response) {
        // Hapus refresh token dari DB
        refreshTokenRepository.deleteByUserUserId(userId);

        // Hapus cookie access token
        clearCookie(response, "access_token");
        clearCookie(response, "refresh_token");
    }

    // ─── REFRESH TOKEN ────────────────────────────────────────────────────────────
    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue, HttpServletResponse response) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadRequestException("Refresh token tidak valid"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadRequestException("Refresh token sudah expired, silakan login ulang");
        }

        User user = refreshToken.getUser();

        // Hapus refresh token lama
        refreshTokenRepository.delete(refreshToken);

        return generateTokensAndBuildResponse(user, response);
    }

    // ─── FORGOT PASSWORD ─────────────────────────────────────────────────────────
    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email tidak ditemukan"));

        // Hapus token lama jika ada
        passwordResetTokenRepository.deleteByUserUserId(user.getUserId());

        // Buat token baru
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiredAt(LocalDateTime.now().plusSeconds(resetPasswordExpiration / 1000))
                .build();
        passwordResetTokenRepository.save(resetToken);

        // Kirim email
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset Password - Katerly");
        message.setText("Halo " + user.getNamaPemilik() + ",\n\n"
                + "Klik link berikut untuk reset password kamu:\n"
                + resetLink + "\n\n"
                + "Link ini akan expired dalam 1 jam.\n\n"
                + "Jika kamu tidak meminta reset password, abaikan email ini.\n\n"
                + "Salam,\nTim Katerly");
        mailSender.send(message);
    }

    // ─── RESET PASSWORD ───────────────────────────────────────────────────────────
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new BadRequestException("Password dan konfirmasi password tidak sama");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(req.getToken())
                .orElseThrow(() -> new BadRequestException("Token tidak valid"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BadRequestException("Token sudah expired, silakan request ulang");
        }

        if (resetToken.getIsUsed()) {
            throw new BadRequestException("Token sudah digunakan");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        // Tandai token sudah digunakan
        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Hapus semua refresh token user agar login ulang
        refreshTokenRepository.deleteByUserUserId(user.getUserId());
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────────
    private AuthResponse generateTokensAndBuildResponse(User user, HttpServletResponse response) {
        // Generate access token
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getUserId());

        // Generate refresh token
        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiredAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        // Set access token ke HttpOnly Cookie
        setHttpOnlyCookie(response, "access_token", accessToken, (int) (refreshTokenExpiration / 1000));

        // Set refresh token ke HttpOnly Cookie
        setHttpOnlyCookie(response, "refresh_token", refreshTokenValue, (int) (refreshTokenExpiration / 1000));

        boolean hasBusinessProfile = businessProfileRepository.existsByUserUserId(user.getUserId());

        return AuthResponse.builder()
                .userId(user.getUserId())
                .namaPemilik(user.getNamaPemilik())
                .email(user.getEmail())
                .isPremium(user.isPremium())
                .hasBusinessProfile(hasBusinessProfile)
                .build();
    }

    private void setHttpOnlyCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);   // Tidak bisa diakses JavaScript
        cookie.setSecure(false);    // Set true saat production (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
package com.katerly.catering.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.katerly.catering.dto.request.GoogleTokenRequest;
import com.katerly.catering.dto.response.AuthResponse;
import com.katerly.catering.entity.RefreshToken;
import com.katerly.catering.entity.User;
import com.katerly.catering.exception.BadRequestException;
import com.katerly.catering.repository.BusinessProfileRepository;
import com.katerly.catering.repository.RefreshTokenRepository;
import com.katerly.catering.repository.UserRepository;
import com.katerly.catering.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    // ─── VERIFY GOOGLE TOKEN FROM FRONTEND ───────────────────────────────────────
    @Transactional
    public AuthResponse loginWithGoogleToken(GoogleTokenRequest req, HttpServletResponse response) {
        try {
            // Verifikasi token dari Google
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(req.getToken());
            if (idToken == null) {
                throw new BadRequestException("Token Google tidak valid");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // Cek apakah user sudah ada
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                // Register otomatis
                user = User.builder()
                        .email(email)
                        .namaPemilik(name)
                        .googleId(googleId)
                        .build();
                user = userRepository.save(user);
            } else if (user.getGoogleId() == null) {
                // Link Google ke akun existing
                user.setGoogleId(googleId);
                user = userRepository.save(user);
            }

            // Generate tokens dan set cookie
            String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getUserId());
            String refreshTokenValue = UUID.randomUUID().toString();

            RefreshToken refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(refreshTokenValue)
                    .expiredAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                    .build();
            refreshTokenRepository.save(refreshToken);

            setHttpOnlyCookie(response, "access_token", accessToken, (int) (refreshTokenExpiration / 1000));
            setHttpOnlyCookie(response, "refresh_token", refreshTokenValue, (int) (refreshTokenExpiration / 1000));

            boolean hasBusinessProfile = businessProfileRepository.existsByUserUserId(user.getUserId());

            return AuthResponse.builder()
                    .userId(user.getUserId())
                    .namaPemilik(user.getNamaPemilik())
                    .email(user.getEmail())
                    .isPremium(user.isPremium())
                    .hasBusinessProfile(hasBusinessProfile)
                    .build();

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Gagal verifikasi token Google: " + e.getMessage());
        }
    }

    private void setHttpOnlyCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
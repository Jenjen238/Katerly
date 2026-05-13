package com.katerly.catering.security;

import com.katerly.catering.entity.User;
import com.katerly.catering.repository.BusinessProfileRepository;
import com.katerly.catering.repository.RefreshTokenRepository;
import com.katerly.catering.repository.UserRepository;
import com.katerly.catering.entity.RefreshToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

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

        // Set cookie access token
        setHttpOnlyCookie(response, "access_token", accessToken, (int) (refreshTokenExpiration / 1000));

        // Set cookie refresh token
        setHttpOnlyCookie(response, "refresh_token", refreshTokenValue, (int) (refreshTokenExpiration / 1000));

        // Redirect ke frontend
        boolean hasBusinessProfile = businessProfileRepository.existsByUserUserId(user.getUserId());
        String redirectUrl = hasBusinessProfile
                ? frontendUrl + "/dashboard"
                : frontendUrl + "/onboarding";

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void setHttpOnlyCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set true saat production (HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
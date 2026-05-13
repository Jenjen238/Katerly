package com.katerly.catering.service;

import com.katerly.catering.entity.User;
import com.katerly.catering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String googleId = oAuth2User.getAttribute("sub");
        String email    = oAuth2User.getAttribute("email");
        String name     = oAuth2User.getAttribute("name");

        // Cek apakah user sudah ada berdasarkan email
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // User baru — register otomatis
            user = User.builder()
                    .email(email)
                    .namaPemilik(name)
                    .googleId(googleId)
                    .build();
            userRepository.save(user);
        } else if (user.getGoogleId() == null) {
            // User sudah ada tapi belum link Google — link sekarang
            user.setGoogleId(googleId);
            userRepository.save(user);
        }

        return oAuth2User;
    }
}
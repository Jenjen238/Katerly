package com.katerly.catering.security;

import com.katerly.catering.entity.User;
import com.katerly.catering.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

@Autowired
private UserRepository userRepository;

@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .authorities(List.of(new SimpleGrantedAuthority(
                        user.isPremium() ? "ROLE_PREMIUM" : "ROLE_USER")))
                .build();
        }
}
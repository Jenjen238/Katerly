package com.katerly.catering.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "nama_pemilik", nullable = false, length = 100)
    private String namaPemilik;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Nullable karena user login via Google tidak punya password
    @Column(nullable = true, length = 255)
    private String password;

    // Untuk login via Google OAuth (diisi nanti)
    @Column(name = "google_id", unique = true, length = 255)
    private String googleId;

    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private Boolean isPremium = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
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

    @Column(nullable = true, length = 255)
    private String password;
    //Google id nanti diisi
    @Column(name = "google_id", unique = true, length = 255)
    private String googleId;

    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private boolean premium = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
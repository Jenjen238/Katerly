package com.katerly.catering.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "business_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "nama_usaha", nullable = false, length = 100)
    private String namaUsaha;

    @Column(nullable = false, length = 100)
    private String provinsi;

    @Column(name = "no_whatsapp", length = 20)
    private String noWhatsapp;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String alamat;

    @Column(name = "logo_path", length = 255)
    private String logoPath;

    // Preferensi Harga
    @Column(name = "margin_default", precision = 5, scale = 2)
    private BigDecimal marginDefault;

    @Column(name = "mata_uang", length = 10)
    @Builder.Default
    private String matauang = "IDR";

    @Column(name = "pajak_default", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal pajakDefault = BigDecimal.ZERO;

    @Column(name = "biaya_pengantaran_default", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal biayaPengantaranDefault = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
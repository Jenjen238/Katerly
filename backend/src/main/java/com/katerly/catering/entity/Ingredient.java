package com.katerly.catering.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Long ingredientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String nama;

    @Column(nullable = false, length = 20)
    private String satuan; // kg, liter, gram, pcs, dll

    @Column(name = "harga_per_satuan", nullable = false, precision = 15, scale = 2)
    private BigDecimal hargaPerSatuan;

    // Disimpan otomatis saat user edit harga, untuk hitung tren naik/turun
    @Column(name = "harga_sebelumnya", precision = 15, scale = 2)
    private BigDecimal hargaSebelumnya;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
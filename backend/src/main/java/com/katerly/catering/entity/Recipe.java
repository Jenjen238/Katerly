package com.katerly.catering.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Long recipeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "nama_resep", nullable = false, length = 100)
    private String namaResep;

    @Column(name = "jumlah_porsi", nullable = false)
    private Integer jumlahPorsi;

    @Column(name = "margin", nullable = false, precision = 5, scale = 2)
    private BigDecimal margin;

    // Jika diisi, HPP ini yang dipakai (prioritas utama)
    @Column(name = "hpp_manual", precision = 15, scale = 2)
    private BigDecimal hppManual;

    // HPP hasil kalkulasi dari bahan, atau sama dengan hpp_manual jika diisi
    @Column(name = "hpp_final", precision = 15, scale = 2)
    private BigDecimal hppFinal;

    // Harga jual = hpp_final + margin
    @Column(name = "harga_jual", precision = 15, scale = 2)
    private BigDecimal hargaJual;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecipeIngredient> recipeIngredients = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
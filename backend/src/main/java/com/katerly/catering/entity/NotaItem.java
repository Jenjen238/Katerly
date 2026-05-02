package com.katerly.catering.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "nota_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nota_item_id")
    private Long notaItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_id", nullable = false)
    private Nota nota;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "jumlah_porsi", nullable = false)
    private Integer jumlahPorsi;

    // Snapshot HPP saat nota dibuat, agar tidak berubah jika harga bahan berubah
    @Column(name = "hpp_per_porsi", nullable = false, precision = 15, scale = 2)
    private BigDecimal hppPerPorsi;

    @Column(name = "harga_jual_per_porsi", nullable = false, precision = 15, scale = 2)
    private BigDecimal hargaJualPerPorsi;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;
}
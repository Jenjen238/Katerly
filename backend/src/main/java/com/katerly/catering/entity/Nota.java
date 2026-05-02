package com.katerly.catering.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nota {

    public enum Status {
        DRAFT, SELESAI
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nota_id")
    private Long notaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Auto-generate oleh sistem, contoh: INV-0001
    @Column(name = "nomor_invoice", nullable = false, unique = true, length = 20)
    private String nomorInvoice;

    @Column(name = "nama_client", nullable = false, length = 100)
    private String namaClient;

    @Column(name = "no_wa_client", length = 20)
    private String noWaClient;

    @Column(name = "nama_acara", length = 100)
    private String namaAcara;

    @Column(name = "tanggal_acara")
    private LocalDate tanggalAcara;

    @Column(name = "pajak_persen", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal pajakPersen = BigDecimal.ZERO;

    @Column(name = "biaya_pengantaran", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal biayaPengantaran = BigDecimal.ZERO;

    @Column(name = "total_hpp", precision = 15, scale = 2)
    private BigDecimal totalHpp;

    @Column(name = "total_harga_jual", precision = 15, scale = 2)
    private BigDecimal totalHargaJual;

    @Column(name = "total_profit", precision = 15, scale = 2)
    private BigDecimal totalProfit;

    @Column(name = "margin_aktual", precision = 5, scale = 2)
    private BigDecimal marginAktual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.DRAFT;

    @OneToMany(mappedBy = "nota", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NotaItem> notaItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
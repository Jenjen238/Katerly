package com.katerly.catering.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class NotaResponse {

    private Long notaId;
    private String nomorInvoice;
    private String namaClient;
    private String noWaClient;
    private String namaAcara;
    private LocalDate tanggalAcara;
    private BigDecimal pajakPersen;
    private BigDecimal biayaPengantaran;
    private BigDecimal totalHpp;
    private BigDecimal totalHargaJual;
    private BigDecimal totalProfit;
    private BigDecimal marginAktual;
    private String status;
    private List<NotaItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class NotaItemResponse {
        private Long notaItemId;
        private Long recipeId;
        private String namaResep;
        private Integer jumlahPorsi;
        private BigDecimal hppPerPorsi;
        private BigDecimal hargaJualPerPorsi;
        private BigDecimal subtotal;
    }
}
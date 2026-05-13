package com.katerly.catering.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardResponse {

    // Summary cards
    private BigDecimal totalPendapatan;
    private BigDecimal totalKeuntungan;
    private BigDecimal marginRataRata;
    private Long totalNota;

    // Persentase perubahan dibanding bulan lalu
    private BigDecimal pendapatanChangePercent;
    private BigDecimal keuntunganChangePercent;
    private BigDecimal marginChangePercent;
    private BigDecimal notaChangePercent;

    // Grafik keuntungan per hari dalam bulan ini
    private List<KeuntunganHarian> keuntunganBulanIni;

    // Menu paling untung
    private List<MenuProfitability> menuPalingUntung;

    // Menu terakhir dipakai
    private List<MenuTerakhir> menuTerakhir;

    // Nota terbaru
    private List<NotaSummary> notaTerbaru;

    @Data @Builder
    public static class KeuntunganHarian {
        private String tanggal;
        private BigDecimal keuntungan;
    }

    @Data @Builder
    public static class MenuProfitability {
        private String namaResep;
        private Integer totalPorsi;
        private BigDecimal totalPendapatan;
        private BigDecimal totalHpp;
        private BigDecimal profit;
        private BigDecimal marginPersen;
    }

    @Data @Builder
    public static class MenuTerakhir {
        private String namaResep;
        private Integer totalPorsi;
    }

    @Data @Builder
    public static class NotaSummary {
        private String nomorInvoice;
        private String namaClient;
        private BigDecimal totalHargaJual;
        private BigDecimal marginAktual;
        private String status;
        private String tanggal;
    }
}
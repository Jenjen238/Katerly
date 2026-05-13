package com.katerly.catering.service;

import com.katerly.catering.dto.response.DashboardResponse;
import com.katerly.catering.entity.*;
import com.katerly.catering.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final NotaRepository notaRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM", new Locale("id"));

    public DashboardResponse getDashboard(Long userId, int year, int month) {
        // Ambil nota bulan ini dan bulan lalu
        LocalDate startBulanIni = LocalDate.of(year, month, 1);
        LocalDate endBulanIni = startBulanIni.withDayOfMonth(startBulanIni.lengthOfMonth());
        LocalDate startBulanLalu = startBulanIni.minusMonths(1);
        LocalDate endBulanLalu = startBulanIni.minusDays(1);

        List<Nota> notaBulanIni = getNotaSelesai(userId, startBulanIni, endBulanIni);
        List<Nota> notaBulanLalu = getNotaSelesai(userId, startBulanLalu, endBulanLalu);

        // ─── SUMMARY CARDS ────────────────────────────────────────────────────────
        BigDecimal pendapatanIni = sumPendapatan(notaBulanIni);
        BigDecimal pendapatanLalu = sumPendapatan(notaBulanLalu);

        BigDecimal keuntunganIni = sumKeuntungan(notaBulanIni);
        BigDecimal keuntunganLalu = sumKeuntungan(notaBulanLalu);

        BigDecimal marginIni = avgMargin(notaBulanIni);
        BigDecimal marginLalu = avgMargin(notaBulanLalu);

        long totalNotaIni = notaBulanIni.size();
        long totalNotaLalu = notaBulanLalu.size();

        // ─── KEUNTUNGAN HARIAN ────────────────────────────────────────────────────
        Map<LocalDate, BigDecimal> keuntunganPerHari = new TreeMap<>();
        for (Nota nota : notaBulanIni) {
            LocalDate tgl = nota.getTanggalAcara() != null ? nota.getTanggalAcara() : nota.getCreatedAt().toLocalDate();
            keuntunganPerHari.merge(tgl, nota.getTotalProfit() != null ? nota.getTotalProfit() : BigDecimal.ZERO, BigDecimal::add);
        }
        List<DashboardResponse.KeuntunganHarian> keuntunganHarian = keuntunganPerHari.entrySet().stream()
                .map(e -> DashboardResponse.KeuntunganHarian.builder()
                        .tanggal(e.getKey().format(DATE_FORMAT))
                        .keuntungan(e.getValue().setScale(2, RoundingMode.HALF_UP))
                        .build())
                .collect(Collectors.toList());

        // ─── MENU PALING UNTUNG ───────────────────────────────────────────────────
        Map<Long, MenuAcc> menuMap = new LinkedHashMap<>();
        for (Nota nota : notaBulanIni) {
            for (NotaItem item : nota.getNotaItems()) {
                Long recipeId = item.getRecipe().getRecipeId();
                menuMap.computeIfAbsent(recipeId, k -> new MenuAcc(item.getRecipe().getNamaResep())).add(item);
            }
        }
        List<DashboardResponse.MenuProfitability> menuPalingUntung = menuMap.values().stream()
                .map(a -> {
                    BigDecimal profit = a.totalPendapatan.subtract(a.totalHpp);
                    BigDecimal margin = a.totalPendapatan.compareTo(BigDecimal.ZERO) > 0
                            ? profit.divide(a.totalPendapatan, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return DashboardResponse.MenuProfitability.builder()
                            .namaResep(a.namaResep)
                            .totalPorsi(a.totalPorsi)
                            .totalPendapatan(a.totalPendapatan.setScale(2, RoundingMode.HALF_UP))
                            .totalHpp(a.totalHpp.setScale(2, RoundingMode.HALF_UP))
                            .profit(profit.setScale(2, RoundingMode.HALF_UP))
                            .marginPersen(margin)
                            .build();
                })
                .sorted(Comparator.comparing(DashboardResponse.MenuProfitability::getProfit).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // ─── MENU TERAKHIR ────────────────────────────────────────────────────────
        List<DashboardResponse.MenuTerakhir> menuTerakhir = menuMap.values().stream()
                .limit(4)
                .map(a -> DashboardResponse.MenuTerakhir.builder()
                        .namaResep(a.namaResep)
                        .totalPorsi(a.totalPorsi)
                        .build())
                .collect(Collectors.toList());

        // ─── NOTA TERBARU ─────────────────────────────────────────────────────────
        List<DashboardResponse.NotaSummary> notaTerbaru = notaRepository
                .findByUserUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(5)
                .map(n -> DashboardResponse.NotaSummary.builder()
                        .nomorInvoice(n.getNomorInvoice())
                        .namaClient(n.getNamaClient())
                        .totalHargaJual(n.getTotalHargaJual())
                        .marginAktual(n.getMarginAktual())
                        .status(n.getStatus().name())
                        .tanggal(n.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("id"))))
                        .build())
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalPendapatan(pendapatanIni.setScale(2, RoundingMode.HALF_UP))
                .totalKeuntungan(keuntunganIni.setScale(2, RoundingMode.HALF_UP))
                .marginRataRata(marginIni)
                .totalNota(totalNotaIni)
                .pendapatanChangePercent(changePercent(pendapatanIni, pendapatanLalu))
                .keuntunganChangePercent(changePercent(keuntunganIni, keuntunganLalu))
                .marginChangePercent(changePercent(marginIni, marginLalu))
                .notaChangePercent(changePercent(BigDecimal.valueOf(totalNotaIni), BigDecimal.valueOf(totalNotaLalu)))
                .keuntunganBulanIni(keuntunganHarian)
                .menuPalingUntung(menuPalingUntung)
                .menuTerakhir(menuTerakhir)
                .notaTerbaru(notaTerbaru)
                .build();
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────────
    private List<Nota> getNotaSelesai(Long userId, LocalDate start, LocalDate end) {
        return notaRepository.findByUserUserIdAndStatus(userId, Nota.Status.SELESAI)
                .stream()
                .filter(n -> {
                    LocalDate tgl = n.getTanggalAcara() != null ? n.getTanggalAcara() : n.getCreatedAt().toLocalDate();
                    return !tgl.isBefore(start) && !tgl.isAfter(end);
                })
                .collect(Collectors.toList());
    }

    private BigDecimal sumPendapatan(List<Nota> notas) {
        return notas.stream()
                .map(n -> n.getTotalHargaJual() != null ? n.getTotalHargaJual() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumKeuntungan(List<Nota> notas) {
        return notas.stream()
                .map(n -> n.getTotalProfit() != null ? n.getTotalProfit() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal avgMargin(List<Nota> notas) {
        if (notas.isEmpty()) return BigDecimal.ZERO;
        BigDecimal total = notas.stream()
                .map(n -> n.getMarginAktual() != null ? n.getMarginAktual() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(notas.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal changePercent(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static class MenuAcc {
        String namaResep;
        int totalPorsi = 0;
        BigDecimal totalPendapatan = BigDecimal.ZERO;
        BigDecimal totalHpp = BigDecimal.ZERO;

        MenuAcc(String namaResep) { this.namaResep = namaResep; }

        void add(NotaItem item) {
            totalPorsi += item.getJumlahPorsi();
            totalPendapatan = totalPendapatan.add(item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO);
            totalHpp = totalHpp.add(item.getHppPerPorsi() != null
                    ? item.getHppPerPorsi().multiply(BigDecimal.valueOf(item.getJumlahPorsi()))
                    : BigDecimal.ZERO);
        }
    }
}
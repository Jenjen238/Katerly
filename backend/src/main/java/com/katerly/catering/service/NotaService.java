package com.katerly.catering.service;

import com.katerly.catering.dto.request.NotaRequest;
import com.katerly.catering.dto.response.NotaResponse;
import com.katerly.catering.entity.*;
import com.katerly.catering.exception.BadRequestException;
import com.katerly.catering.exception.ResourceNotFoundException;
import com.katerly.catering.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotaService {

    private final NotaRepository notaRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    private static final int MAX_NOTA_FREE = 4;

    // ─── CREATE ───────────────────────────────────────────────────────────────────
    @Transactional
    public NotaResponse create(Long userId, NotaRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        // Cek batas nota untuk akun gratis
        if (!user.isPremium() && notaRepository.countByUserUserId(userId) >= MAX_NOTA_FREE) {
            throw new BadRequestException(
                    "Akun gratis hanya bisa membuat " + MAX_NOTA_FREE + " nota. Upgrade ke Premium untuk nota tanpa batas.");
        }

        Nota nota = Nota.builder()
                .user(user)
                .nomorInvoice(generateNomorInvoice(userId))
                .namaClient(req.getNamaClient())
                .noWaClient(req.getNoWaClient())
                .namaAcara(req.getNamaAcara())
                .tanggalAcara(req.getTanggalAcara())
                .pajakPersen(req.getPajakPersen() != null ? req.getPajakPersen() : BigDecimal.ZERO)
                .biayaPengantaran(req.getBiayaPengantaran() != null ? req.getBiayaPengantaran() : BigDecimal.ZERO)
                .notaItems(new ArrayList<>())
                .build();

        // Build nota items
        BigDecimal totalHpp = BigDecimal.ZERO;
        BigDecimal totalHargaJual = BigDecimal.ZERO;

        for (NotaRequest.NotaItemRequest itemReq : req.getItems()) {
            Recipe recipe = recipeRepository.findByRecipeIdAndUserUserId(itemReq.getRecipeId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Resep ID " + itemReq.getRecipeId() + " tidak ditemukan"));

            BigDecimal hppPerPorsi = recipe.getHppFinal() != null ? recipe.getHppFinal() : BigDecimal.ZERO;
            BigDecimal hargaJualPerPorsi = recipe.getHargaJual() != null ? recipe.getHargaJual() : BigDecimal.ZERO;
            BigDecimal subtotal = hargaJualPerPorsi
                    .multiply(BigDecimal.valueOf(itemReq.getJumlahPorsi()))
                    .setScale(2, RoundingMode.HALF_UP);

            NotaItem notaItem = NotaItem.builder()
                    .nota(nota)
                    .recipe(recipe)
                    .jumlahPorsi(itemReq.getJumlahPorsi())
                    .hppPerPorsi(hppPerPorsi)
                    .hargaJualPerPorsi(hargaJualPerPorsi)
                    .subtotal(subtotal)
                    .build();

            nota.getNotaItems().add(notaItem);
            totalHpp = totalHpp.add(hppPerPorsi.multiply(BigDecimal.valueOf(itemReq.getJumlahPorsi())));
            totalHargaJual = totalHargaJual.add(subtotal);
        }

        // Tambah biaya pengantaran
        totalHargaJual = totalHargaJual.add(nota.getBiayaPengantaran());

        // Tambah pajak
        BigDecimal pajak = totalHargaJual
                .multiply(nota.getPajakPersen().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
        totalHargaJual = totalHargaJual.add(pajak);

        // Hitung profit & margin aktual
        BigDecimal totalProfit = totalHargaJual.subtract(totalHpp);
        BigDecimal marginAktual = totalHargaJual.compareTo(BigDecimal.ZERO) > 0
                ? totalProfit.divide(totalHargaJual, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        nota.setTotalHpp(totalHpp.setScale(2, RoundingMode.HALF_UP));
        nota.setTotalHargaJual(totalHargaJual.setScale(2, RoundingMode.HALF_UP));
        nota.setTotalProfit(totalProfit.setScale(2, RoundingMode.HALF_UP));
        nota.setMarginAktual(marginAktual);

        return toResponse(notaRepository.save(nota));
    }

    // ─── UPDATE STATUS ────────────────────────────────────────────────────────────
    @Transactional
    public NotaResponse updateStatus(Long userId, Long notaId, String status) {
        Nota nota = notaRepository.findByNotaIdAndUserUserId(notaId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota tidak ditemukan"));
        try {
            nota.setStatus(Nota.Status.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Status tidak valid. Gunakan: DRAFT atau SELESAI");
        }
        return toResponse(notaRepository.save(nota));
    }

    // ─── GET ALL ──────────────────────────────────────────────────────────────────
    public List<NotaResponse> getAll(Long userId) {
        return notaRepository.findByUserUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ─── GET BY ID ────────────────────────────────────────────────────────────────
    public NotaResponse getById(Long userId, Long notaId) {
        Nota nota = notaRepository.findByNotaIdAndUserUserId(notaId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota tidak ditemukan"));
        return toResponse(nota);
    }

    // ─── SEARCH ───────────────────────────────────────────────────────────────────
    public List<NotaResponse> searchByClient(Long userId, String keyword) {
        return notaRepository.findByUserUserIdAndNamaClientContainingIgnoreCase(userId, keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<NotaResponse> searchByInvoice(Long userId, String keyword) {
        return notaRepository.findByUserUserIdAndNomorInvoiceContainingIgnoreCase(userId, keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────────
    @Transactional
    public void delete(Long userId, Long notaId) {
        Nota nota = notaRepository.findByNotaIdAndUserUserId(notaId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota tidak ditemukan"));
        notaRepository.delete(nota);
    }

    // ─── GENERATE NOMOR INVOICE ───────────────────────────────────────────────────
    private String generateNomorInvoice(Long userId) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = String.valueOf(System.currentTimeMillis() % 100000);
        return String.format("INV-%s-%s-%s", datePart, userId, uniquePart);
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────────
    private NotaResponse toResponse(Nota n) {
        List<NotaResponse.NotaItemResponse> items = n.getNotaItems().stream()
                .map(ni -> NotaResponse.NotaItemResponse.builder()
                        .notaItemId(ni.getNotaItemId())
                        .recipeId(ni.getRecipe().getRecipeId())
                        .namaResep(ni.getRecipe().getNamaResep())
                        .jumlahPorsi(ni.getJumlahPorsi())
                        .hppPerPorsi(ni.getHppPerPorsi())
                        .hargaJualPerPorsi(ni.getHargaJualPerPorsi())
                        .subtotal(ni.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return NotaResponse.builder()
                .notaId(n.getNotaId())
                .nomorInvoice(n.getNomorInvoice())
                .namaClient(n.getNamaClient())
                .noWaClient(n.getNoWaClient())
                .namaAcara(n.getNamaAcara())
                .tanggalAcara(n.getTanggalAcara())
                .pajakPersen(n.getPajakPersen())
                .biayaPengantaran(n.getBiayaPengantaran())
                .totalHpp(n.getTotalHpp())
                .totalHargaJual(n.getTotalHargaJual())
                .totalProfit(n.getTotalProfit())
                .marginAktual(n.getMarginAktual())
                .status(n.getStatus().name())
                .items(items)
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
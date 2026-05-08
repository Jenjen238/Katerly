package com.katerly.catering.service;

import com.katerly.catering.dto.request.IngredientRequest;
import com.katerly.catering.dto.response.IngredientResponse;
import com.katerly.catering.entity.Ingredient;
import com.katerly.catering.entity.User;
import com.katerly.catering.exception.BadRequestException;
import com.katerly.catering.exception.ResourceNotFoundException;
import com.katerly.catering.repository.IngredientRepository;
import com.katerly.catering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;

    // ─── CREATE ───────────────────────────────────────────────────────────────────
    @Transactional
    public IngredientResponse create(Long userId, IngredientRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        if (ingredientRepository.existsByNamaIgnoreCaseAndUserUserId(req.getNama(), userId)) {
            throw new BadRequestException("Bahan dengan nama '" + req.getNama() + "' sudah ada");
        }

        Ingredient ingredient = Ingredient.builder()
                .user(user)
                .nama(req.getNama())
                .satuan(req.getSatuan())
                .hargaPerSatuan(req.getHargaPerSatuan())
                .build();

        return toResponse(ingredientRepository.save(ingredient));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────────
    @Transactional
    public IngredientResponse update(Long userId, Long ingredientId, IngredientRequest req) {
        Ingredient ingredient = ingredientRepository
                .findByIngredientIdAndUserUserId(ingredientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bahan tidak ditemukan"));

        // Simpan harga sebelumnya untuk hitung tren
        ingredient.setHargaSebelumnya(ingredient.getHargaPerSatuan());
        ingredient.setNama(req.getNama());
        ingredient.setSatuan(req.getSatuan());
        ingredient.setHargaPerSatuan(req.getHargaPerSatuan());

        return toResponse(ingredientRepository.save(ingredient));
    }

    // ─── GET ALL ──────────────────────────────────────────────────────────────────
    public List<IngredientResponse> getAll(Long userId) {
        return ingredientRepository.findByUserUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── GET BY ID ────────────────────────────────────────────────────────────────
    public IngredientResponse getById(Long userId, Long ingredientId) {
        Ingredient ingredient = ingredientRepository
                .findByIngredientIdAndUserUserId(ingredientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bahan tidak ditemukan"));
        return toResponse(ingredient);
    }

    // ─── SEARCH ───────────────────────────────────────────────────────────────────
    public List<IngredientResponse> search(Long userId, String keyword) {
        return ingredientRepository
                .findByUserUserIdAndNamaContainingIgnoreCase(userId, keyword)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────────
    @Transactional
    public void delete(Long userId, Long ingredientId) {
        Ingredient ingredient = ingredientRepository
                .findByIngredientIdAndUserUserId(ingredientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bahan tidak ditemukan"));
        ingredientRepository.delete(ingredient);
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────────
    public IngredientResponse toResponse(Ingredient i) {
        BigDecimal trendPersen = null;
        if (i.getHargaSebelumnya() != null
                && i.getHargaSebelumnya().compareTo(BigDecimal.ZERO) > 0) {
            trendPersen = i.getHargaPerSatuan()
                    .subtract(i.getHargaSebelumnya())
                    .divide(i.getHargaSebelumnya(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return IngredientResponse.builder()
                .ingredientId(i.getIngredientId())
                .nama(i.getNama())
                .satuan(i.getSatuan())
                .hargaPerSatuan(i.getHargaPerSatuan())
                .hargaSebelumnya(i.getHargaSebelumnya())
                .trendPersen(trendPersen)
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }
}
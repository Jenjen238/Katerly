package com.katerly.catering.service;

import com.katerly.catering.dto.request.BusinessProfileRequest;
import com.katerly.catering.dto.response.BusinessProfileResponse;
import com.katerly.catering.entity.BusinessProfile;
import com.katerly.catering.entity.User;
import com.katerly.catering.exception.ResourceNotFoundException;
import com.katerly.catering.repository.BusinessProfileRepository;
import com.katerly.catering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
@RequiredArgsConstructor
public class BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;
    private final UserRepository userRepository;

    @Value("${katerly.upload.dir}")
    private String uploadDir;

    // ─── CREATE OR UPDATE PROFILE
    // ─────────────────────────────────────────────────
    @Transactional
    public BusinessProfileResponse saveProfile(Long userId, BusinessProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        BusinessProfile profile = businessProfileRepository.findByUserUserId(userId)
                .orElse(BusinessProfile.builder().user(user).build());

        profile.setNamaUsaha(req.getNamaUsaha());
        profile.setProvinsi(req.getProvinsi());
        profile.setNoWhatsapp(req.getNoWhatsapp());
        profile.setEmail(req.getEmail());
        profile.setAlamat(req.getAlamat());
        profile.setMarginDefault(req.getMarginDefault());
        profile.setMatauang(req.getMatauang() != null ? req.getMatauang() : "IDR");
        profile.setPajakDefault(req.getPajakDefault());
        profile.setBiayaPengantaranDefault(req.getBiayaPengantaranDefault());

        return toResponse(businessProfileRepository.save(profile));
    }

    // ─── GET PROFILE
    // ──────────────────────────────────────────────────────────────
    public BusinessProfileResponse getProfile(Long userId) {
        BusinessProfile profile = businessProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil bisnis belum dibuat"));
        return toResponse(profile);
    }

    // ─── UPLOAD LOGO
    // ──────────────────────────────────────────────────────────────
    @Transactional
    public BusinessProfileResponse uploadLogo(Long userId, MultipartFile file) throws IOException {
        BusinessProfile profile = businessProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil bisnis belum dibuat"));

        // Validasi tipe file
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File harus berupa gambar");
        }

        // Buat folder jika belum ada
        Files.createDirectories(Paths.get(uploadDir));

        // Hapus logo lama jika ada
        if (profile.getLogoPath() != null) {
            Files.deleteIfExists(Paths.get(profile.getLogoPath()));
        }

        // Simpan logo baru
        String extension = getExtension(file.getOriginalFilename());
        String filename = "logo_" + userId + "_" + System.currentTimeMillis() + extension;
        Path filePath = Paths.get(uploadDir, filename);
        Files.write(filePath, file.getBytes());

        profile.setLogoPath(filePath.toString());
        return toResponse(businessProfileRepository.save(profile));
    }

    // ─── HELPER
    // ───────────────────────────────────────────────────────────────────
    private String getExtension(String filename) {
        if (filename == null)
            return ".png";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : ".png";
    }

    private BusinessProfileResponse toResponse(BusinessProfile p) {
        return BusinessProfileResponse.builder()
                .profileId(p.getProfileId())
                .namaUsaha(p.getNamaUsaha())
                .provinsi(p.getProvinsi())
                .noWhatsapp(p.getNoWhatsapp())
                .email(p.getEmail())
                .alamat(p.getAlamat())
                .logoPath(p.getLogoPath())
                .marginDefault(p.getMarginDefault())
                .matauang(p.getMatauang())
                .pajakDefault(p.getPajakDefault())
                .biayaPengantaranDefault(p.getBiayaPengantaranDefault())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
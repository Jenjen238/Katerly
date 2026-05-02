package com.katerly.catering.repository;

import com.katerly.catering.entity.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {
    List<Nota> findByUserUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Nota> findByNotaIdAndUserUserId(Long notaId, Long userId);
    boolean existsByNomorInvoice(String nomorInvoice);
    long countByUserUserId(Long userId);

    // Untuk fitur search di halaman riwayat
    List<Nota> findByUserUserIdAndNamaClientContainingIgnoreCase(Long userId, String namaClient);
    List<Nota> findByUserUserIdAndNomorInvoiceContainingIgnoreCase(Long userId, String nomorInvoice);

    // Untuk dashboard
    List<Nota> findByUserUserIdAndStatus(Long userId, Nota.Status status);
}
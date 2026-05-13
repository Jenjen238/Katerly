package com.katerly.catering.controller;

import com.katerly.catering.dto.request.NotaRequest;
import com.katerly.catering.dto.response.ApiResponse;
import com.katerly.catering.security.JwtUtil;
import com.katerly.catering.service.NotaService;
import com.katerly.catering.service.NotaPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/notas")
@RequiredArgsConstructor
@Tag(name = "Nota", description = "Kelola nota & generate PDF")
public class NotaController {

    private final NotaService notaService;
    private final NotaPdfService notaPdfService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "Ambil semua nota")
    public ResponseEntity<ApiResponse<?>> getAll(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                notaService.getAll(getUserId(request))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ambil detail nota")
    public ResponseEntity<ApiResponse<?>> getById(
            HttpServletRequest request,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                notaService.getById(getUserId(request), id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search nota berdasarkan nama client atau nomor invoice")
    public ResponseEntity<ApiResponse<?>> search(
            HttpServletRequest request,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "client") String type) {
        Long userId = getUserId(request);
        if ("invoice".equalsIgnoreCase(type)) {
            return ResponseEntity.ok(ApiResponse.success("Berhasil",
                    notaService.searchByInvoice(userId, keyword)));
        }
        return ResponseEntity.ok(ApiResponse.success("Berhasil",
                notaService.searchByClient(userId, keyword)));
    }

    @PostMapping
    @Operation(summary = "Buat nota baru")
    public ResponseEntity<ApiResponse<?>> create(
            HttpServletRequest request,
            @Valid @RequestBody NotaRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Nota berhasil dibuat",
                notaService.create(getUserId(request), req)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update status nota: DRAFT atau SELESAI")
    public ResponseEntity<ApiResponse<?>> updateStatus(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("Status berhasil diperbarui",
                notaService.updateStatus(getUserId(request), id, status)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hapus nota")
    public ResponseEntity<ApiResponse<?>> delete(
            HttpServletRequest request,
            @PathVariable Long id) {
        notaService.delete(getUserId(request), id);
        return ResponseEntity.ok(ApiResponse.success("Nota berhasil dihapus"));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Download PDF nota")
    public ResponseEntity<byte[]> downloadPdf(
            HttpServletRequest request,
            @PathVariable Long id) throws IOException {
        byte[] pdf = notaPdfService.generatePdf(getUserId(request), id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("nota-" + id + ".pdf").build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    // ─── Helper ──────────────────────────────────────────────────────────────────
    private Long getUserId(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return jwtUtil.extractUserId(cookie.getValue());
                }
            }
        }
        return null;
    }
}
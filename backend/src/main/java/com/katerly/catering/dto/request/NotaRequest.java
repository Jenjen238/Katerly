package com.katerly.catering.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class NotaRequest {

    @NotBlank(message = "Nama client wajib diisi")
    @Size(max = 100, message = "Nama client maksimal 100 karakter")
    private String namaClient;

    @Size(max = 20, message = "No WhatsApp maksimal 20 karakter")
    private String noWaClient;

    @Size(max = 100, message = "Nama acara maksimal 100 karakter")
    private String namaAcara;

    @NotNull(message = "Tanggal acara wajib diisi")
    private LocalDate tanggalAcara;

    @DecimalMin(value = "0", message = "Pajak tidak boleh negatif")
    @DecimalMax(value = "100", message = "Pajak maksimal 100%")
    private BigDecimal pajakPersen;

    @DecimalMin(value = "0", message = "Biaya pengantaran tidak boleh negatif")
    private BigDecimal biayaPengantaran;

    @NotEmpty(message = "Minimal 1 resep harus dipilih")
    private List<NotaItemRequest> items;

    @Data
    public static class NotaItemRequest {

        @NotNull(message = "ID resep wajib diisi")
        private Long recipeId;

        @NotNull(message = "Jumlah porsi wajib diisi")
        @Min(value = 1, message = "Jumlah porsi minimal 1")
        private Integer jumlahPorsi;
    }
}
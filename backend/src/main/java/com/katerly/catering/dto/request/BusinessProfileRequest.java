package com.katerly.catering.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BusinessProfileRequest {

    @NotBlank(message = "Nama usaha wajib diisi")
    @Size(max = 100, message = "Nama usaha maksimal 100 karakter")
    private String namaUsaha;

    @Size(max = 100, message = "Provinsi maksimal 100 karakter")
    private String provinsi;

    @Size(max = 20, message = "No WhatsApp maksimal 20 karakter")
    private String noWhatsapp;

    @Email(message = "Format email tidak valid")
    @Size(max = 150, message = "Email maksimal 150 karakter")
    private String email;

    @Size(max = 255, message = "Alamat maksimal 255 karakter")
    private String alamat;

    @NotNull(message = "Margin default wajib diisi")
    @DecimalMin(value = "0.01", message = "Margin minimal 0.01%")
    @DecimalMax(value = "100.00", message = "Margin maksimal 100%")
    private BigDecimal marginDefault;

    @Size(max = 10, message = "Mata uang maksimal 10 karakter")
    private String matauang;

    @DecimalMin(value = "0", message = "Pajak tidak boleh negatif")
    @DecimalMax(value = "100.00", message = "Pajak maksimal 100%")
    private BigDecimal pajakDefault;

    @DecimalMin(value = "0", message = "Biaya pengantaran tidak boleh negatif")
    private BigDecimal biayaPengantaranDefault;
}
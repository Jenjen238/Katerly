package com.katerly.catering.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BusinessProfileResponse {

    private Long profileId;
    private String namaUsaha;
    private String kota;
    private String noWhatsapp;
    private String email;
    private String alamat;
    private String logoPath;
    private BigDecimal marginDefault;
    private String matauang;
    private BigDecimal pajakDefault;
    private BigDecimal biayaPengantaranDefault;
    private LocalDateTime updatedAt;
}
package com.katerly.catering.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleTokenRequest {

    @NotBlank(message = "Token wajib diisi")
    private String token; // ID token dari Google
}
package com.katerly.catering.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    //Access token disimpan di HttpOnly Cookie
    private Long userId;
    private String namaPemilik;
    private String email;
    private boolean isPremium;
    private boolean hasBusinessProfile;
}
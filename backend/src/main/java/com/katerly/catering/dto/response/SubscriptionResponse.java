package com.katerly.catering.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionResponse {

    private Long subscriptionId;
    private String midtransOrderId;
    private String midtransTransactionId;
    private String status;
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;

    // Snap token untuk frontend Midtrans
    private String snapToken;

    // URL redirect ke halaman pembayaran Midtrans
    private String paymentUrl;
}
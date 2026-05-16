package com.katerly.catering.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class NotaListResponse {

    private Long totalNota;
    private BigDecimal totalProfit;
    private BigDecimal marginRataRata;
    private List<NotaResponse> notas;
}

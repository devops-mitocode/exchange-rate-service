package com.mycompany.exchange_rate_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateResponseDTO {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
}
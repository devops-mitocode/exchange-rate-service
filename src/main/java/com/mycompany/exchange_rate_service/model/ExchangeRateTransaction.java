package com.mycompany.exchange_rate_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rate_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromCurrency;
    private String toCurrency;
    private BigDecimal amount;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
    private LocalDateTime transactionDate;
}

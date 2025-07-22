package com.mycompany.exchange_rate_service.service;

import com.mycompany.exchange_rate_service.dto.ExchangeRateRequestDTO;
import com.mycompany.exchange_rate_service.dto.ExchangeRateResponseDTO;
import com.mycompany.exchange_rate_service.model.ExchangeRate;
import com.mycompany.exchange_rate_service.model.ExchangeRateTransaction;
import com.mycompany.exchange_rate_service.repository.ExchangeRateRepository;
import com.mycompany.exchange_rate_service.repository.ExchangeRateTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateTransactionRepository exchangeRateTransactionRepository;
    private final MathJsService mathJsService;

    @Override
    public ExchangeRateResponseDTO calculateExchangeRate(ExchangeRateRequestDTO request) {
        String fromCurrency = request.getFromCurrency().toUpperCase();
        String toCurrency = request.getToCurrency().toUpperCase();
        BigDecimal amount = request.getAmount();

        log.info("Calculando conversión: {} {} → {}", amount, fromCurrency, toCurrency);

        String currencyPair = fromCurrency + "_" + toCurrency;
        Optional<ExchangeRate> directRateOpt = exchangeRateRepository.findById(currencyPair);

        BigDecimal exchangeRate;

        if (directRateOpt.isPresent()) {
            exchangeRate = directRateOpt.get().getRate();
            log.info("Tipo de cambio directo encontrado: {} = {}", currencyPair, exchangeRate);
        } else {
            // Buscar tipo de cambio inverso
            String inversePair = toCurrency + "_" + fromCurrency;
            Optional<ExchangeRate> inverseRateOpt = exchangeRateRepository.findById(inversePair);

            if (inverseRateOpt.isEmpty()) {
                throw new RuntimeException("Tipo de cambio no disponible para: " + currencyPair + " ni su inverso");
            }

            BigDecimal inverseRate = inverseRateOpt.get().getRate();
            exchangeRate = BigDecimal.ONE.divide(inverseRate, 6, BigDecimal.ROUND_HALF_UP);
            log.info("Tipo de cambio calculado a partir del inverso (1 / {}): {}", inverseRate, exchangeRate);
        }

        BigDecimal convertedAmount = mathJsService.calculateAmount(amount, exchangeRate);

        return ExchangeRateResponseDTO.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .amount(amount.setScale(2, RoundingMode.HALF_UP))
                .exchangeRate(exchangeRate)
                .convertedAmount(convertedAmount)
                .build();
    }

    @Override
    public ExchangeRateTransaction saveExchangeRate(ExchangeRateRequestDTO request) {
        log.info("Proceso save: calculando y guardando transacción");

        ExchangeRateResponseDTO calculated = calculateExchangeRate(request);

        ExchangeRateTransaction transaction = ExchangeRateTransaction.builder()
                .fromCurrency(calculated.getFromCurrency())
                .toCurrency(calculated.getToCurrency())
                .amount(calculated.getAmount())
                .exchangeRate(calculated.getExchangeRate())
                .convertedAmount(calculated.getConvertedAmount())
                .transactionDate(LocalDateTime.now())
                .build();

        ExchangeRateTransaction savedTransaction = exchangeRateTransactionRepository.save(transaction);

        log.info("Transacción registrada con ID: {}", savedTransaction.getId());

        return savedTransaction;
    }
}

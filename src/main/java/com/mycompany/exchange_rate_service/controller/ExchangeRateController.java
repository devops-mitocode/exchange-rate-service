package com.mycompany.exchange_rate_service.controller;


import com.mycompany.exchange_rate_service.dto.ExchangeRateRequestDTO;
import com.mycompany.exchange_rate_service.dto.ExchangeRateResponseDTO;
import com.mycompany.exchange_rate_service.model.ExchangeRate;
import com.mycompany.exchange_rate_service.model.ExchangeRateTransaction;
import com.mycompany.exchange_rate_service.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @PostMapping("/calculate")
    public ResponseEntity<ExchangeRateResponseDTO> calculateExchangeRate(
            @RequestBody ExchangeRateRequestDTO request) {
        try {
            log.info("Endpoint /calculate - {} {} → {}",
                    request.getAmount(), request.getFromCurrency(), request.getToCurrency());

            ExchangeRateResponseDTO response = exchangeRateService.calculateExchangeRate(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error en /calculate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<ExchangeRateTransaction> saveExchangeRate(
            @RequestBody ExchangeRateRequestDTO request) {
        try {
            log.info("Endpoint /save - {} {} → {}",
                    request.getAmount(), request.getFromCurrency(), request.getToCurrency());

            ExchangeRateTransaction savedTransaction = exchangeRateService.saveExchangeRate(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);

        } catch (Exception e) {
            log.error("Error en /save", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

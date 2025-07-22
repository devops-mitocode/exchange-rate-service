package com.mycompany.exchange_rate_service.integration;

import com.mycompany.exchange_rate_service.dto.ExchangeRateRequestDTO;
import com.mycompany.exchange_rate_service.dto.ExchangeRateResponseDTO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExchangeRateService {

    @Autowired
    private com.mycompany.exchange_rate_service.service.ExchangeRateService exchangeRateService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Disabled
    void calcular_TipoCambio_Service() {
        // Arrange
        String fromCurrency = "USD";
        String toCurrency = "PEN";
        BigDecimal amount = BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedRate = BigDecimal.valueOf(3.60).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedConverted = BigDecimal.valueOf(360.00).setScale(2, RoundingMode.HALF_UP);

        var request = new ExchangeRateRequestDTO();
        request.setFromCurrency(fromCurrency);
        request.setToCurrency(toCurrency);
        request.setAmount(amount);

        // Act
        ExchangeRateResponseDTO actual = exchangeRateService.calculateExchangeRate(request);

        // Assert
        assertNotNull(actual);
        assertEquals(request.getFromCurrency(), actual.getFromCurrency());
        assertEquals(request.getToCurrency(), actual.getToCurrency());
        assertEquals(request.getAmount(), actual.getAmount());
        assertEquals(expectedRate, actual.getExchangeRate());
        assertEquals(expectedConverted, actual.getConvertedAmount());
    }

    @Test
    @Disabled
    void calcular_TipoCambio_Controller() {
        // Arrange
        String fromCurrency = "USD";
        String toCurrency = "PEN";
        BigDecimal amount = BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedRate = BigDecimal.valueOf(3.60).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedConverted = BigDecimal.valueOf(360.00).setScale(2, RoundingMode.HALF_UP);

        var request = new ExchangeRateRequestDTO();
        request.setFromCurrency(fromCurrency);
        request.setToCurrency(toCurrency);
        request.setAmount(amount);

        // Act
        ResponseEntity<ExchangeRateResponseDTO> responseEntity =
                restTemplate.postForEntity("/api/exchange-rates/calculate", request, ExchangeRateResponseDTO.class);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        var actual = responseEntity.getBody();
        assertNotNull(actual);

        assertEquals(request.getFromCurrency(), actual.getFromCurrency());
        assertEquals(request.getToCurrency(), actual.getToCurrency());
        assertEquals(request.getAmount(), actual.getAmount());
        assertEquals(expectedRate, actual.getExchangeRate());
        assertEquals(expectedConverted, actual.getConvertedAmount());
    }
}

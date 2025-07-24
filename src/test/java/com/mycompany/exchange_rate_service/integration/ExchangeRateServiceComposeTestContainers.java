package com.mycompany.exchange_rate_service.integration;

import com.mycompany.exchange_rate_service.dto.ExchangeRateRequestDTO;
import com.mycompany.exchange_rate_service.dto.ExchangeRateResponseDTO;
import com.mycompany.exchange_rate_service.service.ExchangeRateService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
public class ExchangeRateServiceComposeTestContainers {

    private static final String POSTGRES_SERVICE = "postgres";
    private static final String WIREMOCK_SERVICE = "wiremock";
    private static final int POSTGRES_PORT = 5432;
    private static final int WIREMOCK_PORT = 8080;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private TestRestTemplate restTemplate;


    @Container
    static ComposeContainer environment = new ComposeContainer(new File("docker-compose.yaml"))
            .withExposedService(POSTGRES_SERVICE, POSTGRES_PORT)
            .withExposedService(WIREMOCK_SERVICE, WIREMOCK_PORT)
            .withStartupTimeout(Duration.ofMinutes(5));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url", () ->
                String.format("jdbc:postgresql://%s:%d/exchange_rate_db",
                        environment.getServiceHost(POSTGRES_SERVICE, POSTGRES_PORT),
                        environment.getServicePort(POSTGRES_SERVICE, POSTGRES_PORT)));

        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");

        // WireMock
        registry.add("mathjs.api.url", () ->
                String.format("http://%s:%d/v4/",
                        environment.getServiceHost(WIREMOCK_SERVICE, WIREMOCK_PORT),
                        environment.getServicePort(WIREMOCK_SERVICE, WIREMOCK_PORT)));
    }

    @Test
    void debugContainerInfo() {
        System.out.println("=== INFORMACIÓN DE CONTENEDORES ===");

        System.out.println("PostgreSQL:");
        System.out.println("  Host: " + environment.getServiceHost(POSTGRES_SERVICE, POSTGRES_PORT));
        System.out.println("  Puerto: " + environment.getServicePort(POSTGRES_SERVICE, POSTGRES_PORT));
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/exchange_rate_db",
                environment.getServiceHost(POSTGRES_SERVICE, POSTGRES_PORT),
                environment.getServicePort(POSTGRES_SERVICE, POSTGRES_PORT));
        System.out.println("  JDBC URL: " + jdbcUrl);

        System.out.println("WireMock:");
        System.out.println("  Host: " + environment.getServiceHost(WIREMOCK_SERVICE, WIREMOCK_PORT));
        System.out.println("  Puerto: " + environment.getServicePort(WIREMOCK_SERVICE, WIREMOCK_PORT));
        String wiremockUrl = String.format("http://%s:%d",
                environment.getServiceHost(WIREMOCK_SERVICE, WIREMOCK_PORT),
                environment.getServicePort(WIREMOCK_SERVICE, WIREMOCK_PORT));
        System.out.println("  URL: " + wiremockUrl);
    }

    @Test
    @Disabled
    void calcular_TipoCambio_Service() throws InterruptedException {
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
    void calcular_TipoCambio_Controller() throws InterruptedException {
//        Thread.sleep(500000);
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

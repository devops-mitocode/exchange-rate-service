package com.mycompany.exchange_rate_service.integration;

import com.mycompany.exchange_rate_service.dto.ExchangeRateRequestDTO;
import com.mycompany.exchange_rate_service.dto.ExchangeRateResponseDTO;
import com.mycompany.exchange_rate_service.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.*;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
public class ExchangeRateServiceTestContainersIndividual {

    private static final String POSTGRES_IMAGE = "postgres:16.9";
    private static final String WIREMOCK_IMAGE = "wiremock/wiremock:3.13.1";

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withFileSystemBind("src/main/resources/schema.sql",
                    "/docker-entrypoint-initdb.d/01-schema.sql", BindMode.READ_ONLY)
            .withFileSystemBind("src/main/resources/data.sql",
                    "/docker-entrypoint-initdb.d/02-data.sql", BindMode.READ_ONLY)
            .withStartupTimeout(Duration.ofMinutes(3));

    @Container
    static GenericContainer<?> wireMockContainer = new GenericContainer<>(
            DockerImageName.parse(WIREMOCK_IMAGE))
            .withFileSystemBind("src/main/resources/wiremock/mappings",
                    "/home/wiremock/mappings", BindMode.READ_ONLY)
            .withExposedPorts(8080)
            .withCommand("--global-response-templating", "--verbose")
            .withStartupTimeout(Duration.ofMinutes(2))
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("mathjs.api.url", () ->
                "http://"+ wireMockContainer.getHost() + ":" + wireMockContainer.getMappedPort(8080) + "/v4/");
    }

    @Test
    void debugContainerInfo() {
        System.out.println("=== INFORMACIÓN DE CONTENEDORES ===");

        System.out.println("PostgreSQL:");
        System.out.println("  Host: " + postgres.getHost());
        System.out.println("  Puerto: " + postgres.getMappedPort(5432));
        System.out.println("  JDBC URL: " + postgres.getJdbcUrl());

        System.out.println("WireMock:");
        System.out.println("  Host: " + wireMockContainer.getHost());
        System.out.println("  Puerto: " + wireMockContainer.getMappedPort(8080));
        System.out.println("  URL: http://" + wireMockContainer.getHost() + ":" + wireMockContainer.getMappedPort(8080));
    }

    @Test
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

package com.mycompany.exchange_rate_service.integration;

import com.mycompany.exchange_rate_service.dto.ExchangeRateRequestDTO;
import com.mycompany.exchange_rate_service.dto.ExchangeRateResponseDTO;
import com.mycompany.exchange_rate_service.service.ExchangeRateService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
public class ExchangeRateServiceTestContainers {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateServiceTestContainers.class);

//    static Network externalNetwork = Network.builder()
//
//            .name(System.getenv("TESTCONTAINERS_NETWORK_NAME"))  // Nombre de la red existente
//            .build();

    private static final String POSTGRES_IMAGE = "postgres:16.9";
    private static final String DATABASE_NAME = "petclinic";
    private static final String DATABASE_USER = "petclinic";
    private static final String DATABASE_PASSWORD = "petclinic";

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
//            .withNetworkMode(resolveNetworkName())
//            .withNetworkAliases("postgres")
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_USER)
            .withPassword(DATABASE_PASSWORD)
            .withFileSystemBind("src/main/resources/schema.sql",
                    "/docker-entrypoint-initdb.d/01-schema.sql", BindMode.READ_ONLY)
            .withFileSystemBind("src/main/resources/data.sql",
                    "/docker-entrypoint-initdb.d/02-data.sql", BindMode.READ_ONLY)
            .withStartupTimeout(Duration.ofMinutes(3));

    @Container
    static GenericContainer<?> wireMockContainer = new GenericContainer<>(
            DockerImageName.parse("wiremock/wiremock:3.13.1"))
//            .withNetworkMode(resolveNetworkName())
            .withNetworkAliases("wiremock")
            .withFileSystemBind("src/main/resources/wiremock/mappings",
                    "/home/wiremock/mappings", BindMode.READ_ONLY)
            .withExposedPorts(8080)
            .withCommand("--global-response-templating", "--verbose")
            .withStartupTimeout(Duration.ofMinutes(2))
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));
//            .waitingFor(Wait.forHttp("/v4/?expr=100.00*3.60").forStatusCode(200).withStartupTimeout(Duration.ofMinutes(2)));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("mathjs.api.url", () ->
                "http://wiremock:" + wireMockContainer.getMappedPort(8080) + "/v4/");
    }

//    @Container
//    static ComposeContainer environment = new ComposeContainer(new File("docker-compose.test.yml"))
//            .withExposedService("postgres", 5432)
//            .withExposedService("wiremock", 8080)
//            .withStartupTimeout(Duration.ofMinutes(5));


    @Test
    @Disabled
    void verificar_configuracion_wiremock() throws InterruptedException {
        // Esperar extra para asegurar que WireMock esté completamente listo
        Thread.sleep(5000);

        int dynamicPort = wireMockContainer.getMappedPort(8080);

        logger.info("=== VALIDACIÓN DE WIREMOCK ===");
        logger.info("Puerto dinámico asignado: {}", dynamicPort);
        logger.info("Container está ejecutándose: {}", wireMockContainer.isRunning());

        // URLs para validar
        String adminUrl = "http://localhost:" + dynamicPort + "/__admin/mappings";
        String mathJsUrl = "http://localhost:" + dynamicPort + "/v4/?expr=1+1";

        logger.info("URL Admin: {}", adminUrl);
        logger.info("URL MathJS test: {}", mathJsUrl);
        logger.info("Container logs: {}", wireMockContainer.getLogs());
        logger.info("====================================");

        logger.info("=== PROBANDO ADMIN ENDPOINT ===");
        ResponseEntity<String> adminResponse = restTemplate.getForEntity(adminUrl, String.class);
        logger.info("Admin response status: {}", adminResponse.getStatusCode());
        logger.info("Admin response body: {}", adminResponse.getBody());

//        assertEquals(expectedUrl, configuredMathJsUrl, "La URL de WireMock no se configuró correctamente");
    }

    @Test
    void validar_contenido_archivos_mapping() throws Exception {
        Thread.sleep(3000);

        logger.info("=== VALIDACIÓN DETALLADA DE ARCHIVOS MAPPING ===");

        // 1. Listar archivos específicos
        org.testcontainers.containers.Container.ExecResult findResult = wireMockContainer.execInContainer("find", "/home/wiremock/mappings", "-name", "*.json");
        logger.info("Archivos .json encontrados:");
        logger.info(findResult.getStdout());

        // 2. Verificar permisos
        org.testcontainers.containers.Container.ExecResult permissionsResult = wireMockContainer.execInContainer("ls", "-la", "/home/wiremock/mappings");
        logger.info("Permisos de archivos:");
        logger.info(permissionsResult.getStdout());

        // 3. Leer contenido de un archivo específico (si existe)
        try {
            org.testcontainers.containers.Container.ExecResult catResult = wireMockContainer.execInContainer("cat", "/home/wiremock/mappings/mathjs-mappings.json");
            if (catResult.getExitCode() == 0) {
                logger.info("Contenido de mathjs.json:");
                logger.info(catResult.getStdout());
            } else {
                logger.warn("Archivo mathjs.json no encontrado");
            }
        } catch (Exception e) {
            logger.warn("No se pudo leer mathjs.json: {}", e.getMessage());
        }

        // 4. Verificar que WireMock puede leer los archivos
        org.testcontainers.containers.Container.ExecResult wiremockCheck = wireMockContainer.execInContainer("ls", "-la", "/home/wiremock/");
        logger.info("Contenido completo de /home/wiremock/:");
        logger.info(wiremockCheck.getStdout());

        // 5. Verificar logs de WireMock para errores de carga
        logger.info("=== LOGS DE WIREMOCK ===");
        String logs = wireMockContainer.getLogs();
        if (logs.contains("ERROR") || logs.contains("WARN")) {
            logger.warn("Se encontraron warnings/errores en logs de WireMock:");
            logger.warn(logs);
        } else {
            logger.info("No hay errores en logs de WireMock");
        }

        logger.info("===========================================");
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
    void calcular_TipoCambio_Controller() throws InterruptedException {
        Thread.sleep(500000);
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

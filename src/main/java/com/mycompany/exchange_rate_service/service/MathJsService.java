package com.mycompany.exchange_rate_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class MathJsService {

    private final RestTemplate restTemplate;

    @Value("${mathjs.api.url:http://api.mathjs.org/v4/}")
    private String mathJsApiUrl;

    public BigDecimal calculateAmount(BigDecimal amount, BigDecimal exchangeRate) {
        if (amount == null || exchangeRate == null) {
            throw new IllegalArgumentException("Math.js requiere monto y tipo de cambio válidos");
        }

        try {
            // Construimos la expresión y la codificamos para la URL
            String expression = amount + "*" + exchangeRate;
            String encodedExpr = URLEncoder.encode(expression, StandardCharsets.UTF_8);

            String url = mathJsApiUrl + "?expr=" + encodedExpr;

            log.info("Invocando Math.js con: {}", url);

            String resultStr = restTemplate.getForObject(url, String.class);

            log.info("Respuesta de Math.js: {}", resultStr);

            return new BigDecimal(resultStr).setScale(2, BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            log.error("Error al consumir Math.js: {}", e.getMessage());
            throw new RuntimeException("Error al consumir servicio Math.js", e);
        }
    }
}

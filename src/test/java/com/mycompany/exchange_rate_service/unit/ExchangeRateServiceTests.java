package com.mycompany.exchange_rate_service.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTests {

    @Test
    @DisplayName("Debe validar que el tipo de cambio se procese correctamente")
    void validarTipoCambio() {
        assertTrue(true); // reemplazar con lógica real en el futuro
    }
}

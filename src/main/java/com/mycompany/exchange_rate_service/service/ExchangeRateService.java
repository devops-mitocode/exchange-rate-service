package com.mycompany.exchange_rate_service.service;

import com.mycompany.exchange_rate_service.dto.ExchangeRateRequestDTO;
import com.mycompany.exchange_rate_service.dto.ExchangeRateResponseDTO;
import com.mycompany.exchange_rate_service.model.ExchangeRate;
import com.mycompany.exchange_rate_service.model.ExchangeRateTransaction;

public interface ExchangeRateService {

    ExchangeRateResponseDTO calculateExchangeRate(ExchangeRateRequestDTO request);

    ExchangeRateTransaction saveExchangeRate(ExchangeRateRequestDTO request);
}

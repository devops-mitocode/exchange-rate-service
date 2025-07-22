package com.mycompany.exchange_rate_service.repository;

import com.mycompany.exchange_rate_service.model.ExchangeRateTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRateTransactionRepository extends JpaRepository<ExchangeRateTransaction, Long> {
}

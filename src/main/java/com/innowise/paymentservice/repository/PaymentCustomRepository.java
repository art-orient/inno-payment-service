package com.innowise.paymentservice.repository;

import java.math.BigDecimal;
import java.time.Instant;

public interface PaymentCustomRepository {

  BigDecimal getTotalAmountForUser(Long userId, Instant from, Instant to);

  BigDecimal getTotalAmountForAllUsers(Instant from, Instant to);
}

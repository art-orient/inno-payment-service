package com.innowise.paymentservice.repository;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Defines custom aggregation-based operations for payment analytics.
 */
public interface PaymentRepositoryCustom {

  /**
   * Calculates the total payment amount for a specific user within the given time range.
   *
   * @param userId the identifier of the user
   * @param from   the start of the time range (inclusive)
   * @param to     the end of the time range (inclusive)
   * @return the total amount of payments for the user in the given period,
   *         or {@code BigDecimal.ZERO} if no payments are found
   */
  BigDecimal getTotalAmountForUser(Long userId, Instant from, Instant to);

  /**
   * Calculates the total payment amount for all users within the given time range.
   *
   * @param from the start of the time range (inclusive)
   * @param to   the end of the time range (inclusive)
   * @return the total amount of payments in the given period,
   *         or {@code BigDecimal.ZERO} if no payments are found
   */
  BigDecimal getTotalAmountForAllUsers(Instant from, Instant to);
}

package com.innowise.paymentservice.service;

import com.innowise.paymentservice.model.dto.PaymentRequestDto;
import com.innowise.paymentservice.model.dto.PaymentResponseDto;
import com.innowise.paymentservice.model.dto.PaymentSummaryDto;
import com.innowise.paymentservice.model.entity.PaymentStatus;

import java.time.Instant;
import java.util.List;

/**
 * Service interface for managing payment operations.
 * <p>
 * Provides functionality for creating payments, retrieving payment records
 * by various criteria, and calculating aggregated payment statistics.
 */
public interface PaymentService {

  /**
   * Creates a new payment based on the provided request data.
   * <p>
   * The payment status is determined by an external random number API:
   * even numbers result in {@link PaymentStatus#SUCCESS},
   * odd numbers result in {@link PaymentStatus#FAILED}.
   * After persistence, the created payment event is published to Kafka.
   *
   * @param dto the payment creation request containing orderId, userId and amount
   * @return the created payment as a response DTO
   */
  PaymentResponseDto createPayment(PaymentRequestDto dto);

  /**
   * Retrieves all payments associated with the specified user.
   *
   * @param userId the ID of the user whose payments should be retrieved
   * @return list of payment response DTOs
   */
  List<PaymentResponseDto> getPaymentsByUserId(Long userId);

  /**
   * Retrieves all payments associated with the specified order.
   *
   * @param orderId the ID of the order whose payments should be retrieved
   * @return list of payment response DTOs
   */
  List<PaymentResponseDto> getPaymentsByOrderId(Long orderId);

  /**
   * Retrieves all payments with the specified status.
   *
   * @param status the payment status to filter by
   * @return list of payment response DTOs
   */
  List<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status);

  /**
   * Calculates the total payment amount for a specific user
   * within the given time range.
   *
   * @param userId the ID of the user
   * @param from   start of the time interval (inclusive)
   * @param to     end of the time interval (inclusive)
   * @return summary DTO containing the aggregated amount
   */
  PaymentSummaryDto getTotalForUser(Long userId, Instant from, Instant to);

  /**
   * Calculates the total payment amount for all users
   * within the given time range.
   *
   * @param from start of the time interval (inclusive)
   * @param to   end of the time interval (inclusive)
   * @return summary DTO containing the aggregated amount
   */
  PaymentSummaryDto getTotalForAllUsers(Instant from, Instant to);
}

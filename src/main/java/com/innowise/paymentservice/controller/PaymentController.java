package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.model.dto.PaymentRequestDto;
import com.innowise.paymentservice.model.dto.PaymentResponseDto;
import com.innowise.paymentservice.model.dto.PaymentSummaryDto;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  /**
   * Creates a new payment.
   *
   * @param dto request body containing orderId, userId and paymentAmount
   * @return created payment
   */
  @PostMapping
  public PaymentResponseDto createPayment(@Valid @RequestBody PaymentRequestDto dto) {
    return paymentService.createPayment(dto);
  }

  /**
   * Retrieves all payments for a specific user.
   *
   * @param userId user identifier
   * @return list of payments
   */
  @GetMapping("/user/{userId}")
  public List<PaymentResponseDto> getPaymentsByUserId(@PathVariable Long userId) {
    return paymentService.getPaymentsByUserId(userId);
  }

  /**
   * Retrieves all payments for a specific order.
   *
   * @param orderId order identifier
   * @return list of payments
   */
  @GetMapping("/order/{orderId}")
  public List<PaymentResponseDto> getPaymentsByOrderId(@PathVariable Long orderId) {
    return paymentService.getPaymentsByOrderId(orderId);
  }

  /**
   * Retrieves all payments with the specified status.
   *
   * @param status payment status
   * @return list of payments
   */
  @GetMapping("/status/{status}")
  public List<PaymentResponseDto> getPaymentsByStatus(@PathVariable PaymentStatus status) {
    return paymentService.getPaymentsByStatus(status);
  }

  /**
   * Calculates total payment amount for a specific user within a time range.
   *
   * @param userId user identifier
   * @param from   start timestamp
   * @param to     end timestamp
   * @return summary DTO with total amount
   */
  @GetMapping("/summary/user/{userId}")
  public PaymentSummaryDto getTotalForUser(
          @PathVariable Long userId,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
  ) {
    return paymentService.getTotalForUser(userId, from, to);
  }

  /**
   * Calculates total payment amount for all users within a time range.
   *
   * @param from start timestamp
   * @param to   end timestamp
   * @return summary DTO with total amount
   */
  @GetMapping("/summary/all")
  public PaymentSummaryDto getTotalForAllUsers(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
  ) {
    return paymentService.getTotalForAllUsers(from, to);
  }
}
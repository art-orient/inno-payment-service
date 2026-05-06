package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.model.dto.PaymentFilterDto;
import com.innowise.paymentservice.model.dto.PaymentRequestDto;
import com.innowise.paymentservice.model.dto.PaymentResponseDto;
import com.innowise.paymentservice.model.dto.PaymentSummaryDto;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
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
  @ResponseStatus(HttpStatus.CREATED)
  public PaymentResponseDto createPayment(@Valid @RequestBody PaymentRequestDto dto) {
    return paymentService.createPayment(dto);
  }

  /**
   * Retrieves payments using exactly one filter criterion.
   * <p>
   * According to the assignment requirements, a payment search must be performed
   * by only one of the following fields: {@code userId}, {@code orderId}, or {@code status}.
   * This method enforces that rule by validating the {@link PaymentFilterDto} and delegating
   * the request to the appropriate service method based on which field is provided.
   * </p>
   *
   * <p>
   * If more than one filter is provided, or if none are provided, the method throws
   * {@link IllegalArgumentException}, ensuring strict adherence to the single-filter contract.
   * </p>
   *
   * @param filter DTO containing exactly one non-null filter parameter
   * @return list of payments matching the selected filter
   * @throws IllegalArgumentException if zero or more than one filter field is provided
   */
  @GetMapping("/search")
  public List<PaymentResponseDto> searchPayments(@Valid PaymentFilterDto filter) {
    int count = 0;
    if (filter.userId() != null) count++;
    if (filter.orderId() != null) count++;
    if (filter.status() != null) count++;
    if (count != 1) {
      throw new IllegalArgumentException("Exactly one filter must be provided");
    }

    List<PaymentResponseDto> result = new ArrayList<>();
    if (filter.userId() != null) {
      result = paymentService.getPaymentsByUserId(filter.userId());
    } else if (filter.orderId() != null) {
      result = paymentService.getPaymentsByOrderId(filter.orderId());
    } else if (filter.status() != null) {
      result = paymentService.getPaymentsByStatus(filter.status());
    }
    return result;
  }

  /**
   * Returns the total payment amount for the currently authenticated user
   * within the specified time interval.
   * <p>
   * The user identifier is obtained from the authenticated security principal
   * (set by the JWT authentication filter); the endpoint does not accept an
   * explicit userId parameter.
   * </p>
   *
   * @param authentication the Spring Security authentication containing the current user's ID as principal
   * @param from start of the time interval (inclusive)
   * @param to end of the time interval (inclusive)
   * @return aggregated payment summary for the current user in the given interval
   */
  @GetMapping("/summary/user")
  @PreAuthorize("isAuthenticated()")
  public PaymentSummaryDto getTotalSumForCurrentUser(
          Authentication authentication,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
    Long currentUserId = (Long) authentication.getPrincipal();
    return paymentService.getTotalForUser(currentUserId, from, to);
  }

  /**
   * Returns the total payment amount aggregated across all users
   * within the specified time interval.
   * <p>
   * Access to this endpoint is restricted to callers with the ADMIN role,
   * enforced via method-level security.
   * </p>
   *
   * @param from start of the time interval (inclusive)
   * @param to end of the time interval (inclusive)
   * @return aggregated payment summary across all users in the given interval
   */
  @GetMapping("/summary/all")
  @PreAuthorize("hasRole('ADMIN')")
  public PaymentSummaryDto getTotalSumForAllUsers(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
    return paymentService.getTotalForAllUsers(from, to);
  }
}
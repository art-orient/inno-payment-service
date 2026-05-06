package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.model.dto.PaymentFilterDto;
import com.innowise.paymentservice.model.dto.PaymentRequestDto;
import com.innowise.paymentservice.model.dto.PaymentResponseDto;
import com.innowise.paymentservice.model.dto.PaymentSummaryDto;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import com.innowise.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

  @Mock
  private PaymentService paymentService;

  @InjectMocks
  private PaymentController paymentController;

  @Test
  void createPayment_delegatesToServiceAndReturnsResponse() {
    PaymentRequestDto request = new PaymentRequestDto(1L, 10L, BigDecimal.valueOf(100));
    PaymentResponseDto response = new PaymentResponseDto(
            "p1",
            1L,
            10L,
            PaymentStatus.SUCCESS,
            Instant.parse("2024-01-01T10:00:00Z"),
            BigDecimal.valueOf(100)
    );
    when(paymentService.createPayment(request)).thenReturn(response);
    PaymentResponseDto result = paymentController.createPayment(request);
    assertThat(result).isEqualTo(response);
    verify(paymentService).createPayment(request);
  }

  @Test
  void searchPayments_userIdFilter_delegatesToService() {
    PaymentFilterDto filter = new PaymentFilterDto(10L, null, null);
    List<PaymentResponseDto> list = List.of(
            new PaymentResponseDto(
                    "p1",
                    1L,
                    10L,
                    PaymentStatus.SUCCESS,
                    Instant.now(),
                    BigDecimal.valueOf(100)
            )
    );
    when(paymentService.getPaymentsByUserId(10L)).thenReturn(list);
    List<PaymentResponseDto> result = paymentController.searchPayments(filter);
    assertThat(result).isEqualTo(list);
    verify(paymentService).getPaymentsByUserId(10L);
  }

  @Test
  void searchPayments_orderIdFilter_delegatesToService() {
    PaymentFilterDto filter = new PaymentFilterDto(null, 1L, null);
    List<PaymentResponseDto> list = List.of(
            new PaymentResponseDto(
                    "p1",
                    1L,
                    10L,
                    PaymentStatus.SUCCESS,
                    Instant.now(),
                    BigDecimal.valueOf(100)
            )
    );
    when(paymentService.getPaymentsByOrderId(1L)).thenReturn(list);
    List<PaymentResponseDto> result = paymentController.searchPayments(filter);
    assertThat(result).isEqualTo(list);
    verify(paymentService).getPaymentsByOrderId(1L);
  }

  @Test
  void searchPayments_statusFilter_delegatesToService() {
    PaymentFilterDto filter = new PaymentFilterDto(null, null, PaymentStatus.SUCCESS);
    List<PaymentResponseDto> list = List.of(
            new PaymentResponseDto(
                    "p1",
                    1L,
                    10L,
                    PaymentStatus.SUCCESS,
                    Instant.now(),
                    BigDecimal.valueOf(100)
            )
    );
    when(paymentService.getPaymentsByStatus(PaymentStatus.SUCCESS)).thenReturn(list);
    List<PaymentResponseDto> result = paymentController.searchPayments(filter);
    assertThat(result).isEqualTo(list);
    verify(paymentService).getPaymentsByStatus(PaymentStatus.SUCCESS);
  }

  @Test
  void searchPayments_noFilters_throwsIllegalArgumentException() {
    PaymentFilterDto filter = new PaymentFilterDto(null, null, null);
    assertThrows(IllegalArgumentException.class, () -> paymentController.searchPayments(filter));
  }

  @Test
  void searchPayments_multipleFilters_throwsIllegalArgumentException() {
    PaymentFilterDto filter = new PaymentFilterDto(10L, 1L, null);
    assertThrows(IllegalArgumentException.class, () -> paymentController.searchPayments(filter));
  }

  @Test
  void getTotalSumForCurrentUser_delegatesToService() {
    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn(10L);
    Instant from = Instant.parse("2024-01-01T00:00:00Z");
    Instant to = Instant.parse("2024-01-02T00:00:00Z");
    PaymentSummaryDto summary = new PaymentSummaryDto(BigDecimal.valueOf(300));
    when(paymentService.getTotalForUser(10L, from, to)).thenReturn(summary);
    PaymentSummaryDto result = paymentController.getTotalSumForCurrentUser(auth, from, to);
    assertThat(result).isEqualTo(summary);
    verify(paymentService).getTotalForUser(10L, from, to);
  }

  @Test
  void getTotalForAllUsers_delegatesToServiceAndReturnsSummary() {
    PaymentSummaryDto summary = new PaymentSummaryDto(BigDecimal.valueOf(500));
    Instant from = Instant.parse("2024-01-01T00:00:00Z");
    Instant to = Instant.parse("2024-01-02T00:00:00Z");
    when(paymentService.getTotalForAllUsers(from, to)).thenReturn(summary);
    PaymentSummaryDto result = paymentController.getTotalSumForAllUsers(from, to);
    assertThat(result).isEqualTo(summary);
    verify(paymentService).getTotalForAllUsers(from, to);
  }
}
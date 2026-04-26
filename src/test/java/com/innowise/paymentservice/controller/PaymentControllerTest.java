package com.innowise.paymentservice.controller;

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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
  void getPaymentsByUserId_delegatesToServiceAndReturnsList() {
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
    List<PaymentResponseDto> result = paymentController.getPaymentsByUserId(10L);
    assertThat(result).isEqualTo(list);
    verify(paymentService).getPaymentsByUserId(10L);
  }

  @Test
  void getPaymentsByOrderId_delegatesToServiceAndReturnsList() {
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
    List<PaymentResponseDto> result = paymentController.getPaymentsByOrderId(1L);
    assertThat(result).isEqualTo(list);
    verify(paymentService).getPaymentsByOrderId(1L);
  }

  @Test
  void getPaymentsByStatus_delegatesToServiceAndReturnsList() {
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
    List<PaymentResponseDto> result = paymentController.getPaymentsByStatus(PaymentStatus.SUCCESS);
    assertThat(result).isEqualTo(list);
    verify(paymentService).getPaymentsByStatus(PaymentStatus.SUCCESS);
  }

  @Test
  void getTotalForUser_delegatesToServiceAndReturnsSummary() {
    PaymentSummaryDto summary = new PaymentSummaryDto(BigDecimal.valueOf(300));
    Instant from = Instant.parse("2024-01-01T00:00:00Z");
    Instant to = Instant.parse("2024-01-02T00:00:00Z");
    when(paymentService.getTotalForUser(10L, from, to)).thenReturn(summary);
    PaymentSummaryDto result = paymentController.getTotalForUser(10L, from, to);
    assertThat(result).isEqualTo(summary);
    verify(paymentService).getTotalForUser(10L, from, to);
  }

  @Test
  void getTotalForAllUsers_delegatesToServiceAndReturnsSummary() {
    PaymentSummaryDto summary = new PaymentSummaryDto(BigDecimal.valueOf(500));
    Instant from = Instant.parse("2024-01-01T00:00:00Z");
    Instant to = Instant.parse("2024-01-02T00:00:00Z");
    when(paymentService.getTotalForAllUsers(from, to)).thenReturn(summary);
    PaymentSummaryDto result = paymentController.getTotalForAllUsers(from, to);
    assertThat(result).isEqualTo(summary);
    verify(paymentService).getTotalForAllUsers(from, to);
  }
}
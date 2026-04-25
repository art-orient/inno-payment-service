package com.innowise.paymentservice.service;

import com.innowise.paymentservice.model.dto.PaymentRequestDto;
import com.innowise.paymentservice.model.dto.PaymentResponseDto;
import com.innowise.paymentservice.model.dto.PaymentSummaryDto;
import com.innowise.paymentservice.model.entity.PaymentStatus;

import java.time.Instant;
import java.util.List;

public interface PaymentService {

  PaymentResponseDto createPayment(PaymentRequestDto dto);

  List<PaymentResponseDto> getPaymentsByUserId(Long userId);

  List<PaymentResponseDto> getPaymentsByOrderId(Long orderId);

  List<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status);

  PaymentSummaryDto getTotalForUser(Long userId, Instant from, Instant to);

  PaymentSummaryDto getTotalForAllUsers(Instant from, Instant to);
}

package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.kafka.PaymentEvent;
import com.innowise.paymentservice.model.dto.PaymentRequestDto;
import com.innowise.paymentservice.model.dto.PaymentResponseDto;
import com.innowise.paymentservice.model.dto.PaymentSummaryDto;
import com.innowise.paymentservice.model.entity.PaymentDocument;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import com.innowise.paymentservice.ourbox.PaymentOutbox;
import com.innowise.paymentservice.ourbox.PaymentOutboxRepository;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.repository.PaymentRepositoryCustom;
import com.innowise.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentRepositoryCustom paymentAnalytics;
  private final PaymentMapper paymentMapper;
  private final RandomNumberClient randomNumberClient;
  private final PaymentOutboxRepository outboxRepository;

  @Transactional
  @Override
  public PaymentResponseDto createPayment(PaymentRequestDto dto) {
    Integer random = randomNumberClient.getOne();
    PaymentStatus status = (random % 2 == 0) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    PaymentDocument payment = paymentMapper.toDocument(dto);
    payment.setStatus(status);
    payment.setTimestamp(Instant.now());
    PaymentDocument saved = paymentRepository.save(payment);
    PaymentEvent event = new PaymentEvent(saved.getOrderId(), saved.getId(),
            saved.getStatus(), saved.getTimestamp());
    outboxRepository.save(new PaymentOutbox(
            UUID.randomUUID().toString(),
            event,
            Instant.now(),
            false
    ));
    return paymentMapper.toResponseDto(saved);
  }

  @Override
  public List<PaymentResponseDto> getPaymentsByUserId(Long userId) {
    return paymentRepository.findByUserId(userId)
            .stream()
            .map(paymentMapper::toResponseDto)
            .toList();
  }

  @Override
  public List<PaymentResponseDto> getPaymentsByOrderId(Long orderId) {
    return paymentRepository.findByOrderId(orderId)
            .stream()
            .map(paymentMapper::toResponseDto)
            .toList();
  }

  @Override
  public List<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status) {
    return paymentRepository.findByStatus(status)
            .stream()
            .map(paymentMapper::toResponseDto)
            .toList();
  }

  @Override
  public PaymentSummaryDto getTotalForUser(Long userId, Instant from, Instant to) {
    var total = paymentAnalytics.getTotalAmountForUser(userId, from, to);
    return new PaymentSummaryDto(total);
  }

  @Override
  public PaymentSummaryDto getTotalForAllUsers(Instant from, Instant to) {
    var total = paymentAnalytics.getTotalAmountForAllUsers(from, to);
    return new PaymentSummaryDto(total);
  }
}

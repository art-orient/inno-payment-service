package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.dto.PaymentRequestDto;
import com.innowise.paymentservice.model.dto.PaymentResponseDto;
import com.innowise.paymentservice.model.dto.PaymentSummaryDto;
import com.innowise.paymentservice.model.entity.PaymentDocument;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import com.innowise.paymentservice.repository.PaymentCustomRepository;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentCustomRepository paymentCustomRepository;
  private final PaymentMapper paymentMapper;
  private final WebClient randomNumberWebClient;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public PaymentResponseDto createPayment(PaymentRequestDto dto) {
    Integer random = randomNumberWebClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/integers/")
                    .queryParam("num", 1)
                    .queryParam("min", 1)
                    .queryParam("max", 100)
                    .queryParam("col", 1)
                    .queryParam("base", 10)
                    .queryParam("format", "plain")
                    .queryParam("rnd", "new")
                    .build())
            .retrieve()
            .bodyToMono(String.class)
            .map(Integer::parseInt)
            .block();
    PaymentStatus status = (random % 2 == 0) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    PaymentDocument document = paymentMapper.toDocument(dto);
    document.setStatus(status);
    document.setTimestamp(Instant.now());
    PaymentDocument saved = paymentRepository.save(document);
    kafkaTemplate.send("payment-events", saved);
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
    var total = paymentCustomRepository.getTotalAmountForUser(userId, from, to);
    return new PaymentSummaryDto(total);
  }

  @Override
  public PaymentSummaryDto getTotalForAllUsers(Instant from, Instant to) {
    var total = paymentCustomRepository.getTotalAmountForAllUsers(from, to);
    return new PaymentSummaryDto(total);
  }
}

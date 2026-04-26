package com.innowise.paymentservice.service;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.kafka.PaymentProducer;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.dto.PaymentRequestDto;
import com.innowise.paymentservice.model.dto.PaymentResponseDto;
import com.innowise.paymentservice.model.dto.PaymentSummaryDto;
import com.innowise.paymentservice.model.entity.PaymentDocument;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.repository.PaymentRepositoryCustom;
import com.innowise.paymentservice.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private PaymentRepositoryCustom paymentRepositoryCustom;

  @Mock
  private PaymentMapper paymentMapper;

  @Mock
  private PaymentProducer paymentProducer;

  @Mock
  private RandomNumberClient randomNumberClient;

  @InjectMocks
  private PaymentServiceImpl paymentService;

  @Test
  void createPayment_success() {
    PaymentRequestDto dto = new PaymentRequestDto(1L, 10L, BigDecimal.valueOf(100));
    PaymentDocument mapped = new PaymentDocument();
    mapped.setOrderId(1L);
    mapped.setUserId(10L);
    mapped.setPaymentAmount(BigDecimal.valueOf(100));

    PaymentDocument saved = new PaymentDocument();
    saved.setId("p1");
    saved.setOrderId(1L);
    saved.setUserId(10L);
    saved.setPaymentAmount(BigDecimal.valueOf(100));
    saved.setStatus(PaymentStatus.SUCCESS);
    saved.setTimestamp(Instant.now());
    PaymentResponseDto responseDto = new PaymentResponseDto(
            "p1", 1L, 10L, PaymentStatus.SUCCESS, saved.getTimestamp(), BigDecimal.valueOf(100));
    when(randomNumberClient.getOne()).thenReturn(42);
    when(paymentMapper.toDocument(dto)).thenReturn(mapped);
    when(paymentRepository.save(mapped)).thenReturn(saved);
    when(paymentMapper.toResponseDto(saved)).thenReturn(responseDto);
    PaymentResponseDto result = paymentService.createPayment(dto);
    assertThat(result.status()).isEqualTo(PaymentStatus.SUCCESS);
    verify(randomNumberClient).getOne();
    verify(paymentRepository).save(mapped);
    verify(paymentProducer).sendPaymentEvent(argThat(event ->
                    event.orderId().equals(1L)
                            && event.paymentId().equals("p1")
                            && event.status() == PaymentStatus.SUCCESS));
  }

  @Test
  void getPaymentsByUserId() {
    PaymentDocument doc = new PaymentDocument();
    doc.setId("1");
    when(paymentRepository.findByUserId(10L)).thenReturn(List.of(doc));
    when(paymentMapper.toResponseDto(doc)).thenReturn(
            new PaymentResponseDto("1", 1L, 10L, PaymentStatus.SUCCESS, Instant.now(), BigDecimal.TEN)
    );
    var result = paymentService.getPaymentsByUserId(10L);
    assertThat(result).hasSize(1);
  }

  @Test
  void getPaymentsByOrderId() {
    PaymentDocument doc = new PaymentDocument();
    doc.setId("1");
    when(paymentRepository.findByOrderId(5L)).thenReturn(List.of(doc));
    when(paymentMapper.toResponseDto(doc)).thenReturn(
            new PaymentResponseDto("1", 5L, 10L, PaymentStatus.SUCCESS, Instant.now(), BigDecimal.TEN)
    );
    var result = paymentService.getPaymentsByOrderId(5L);
    assertThat(result).hasSize(1);
  }

  @Test
  void getPaymentsByStatus() {
    PaymentDocument doc = new PaymentDocument();
    doc.setId("1");
    when(paymentRepository.findByStatus(PaymentStatus.SUCCESS)).thenReturn(List.of(doc));
    when(paymentMapper.toResponseDto(doc)).thenReturn(
            new PaymentResponseDto("1", 1L, 10L, PaymentStatus.SUCCESS, Instant.now(), BigDecimal.TEN)
    );
    var result = paymentService.getPaymentsByStatus(PaymentStatus.SUCCESS);
    assertThat(result).hasSize(1);
  }

  @Test
  void getTotalForUser() {
    when(paymentRepositoryCustom.getTotalAmountForUser(any(), any(), any()))
            .thenReturn(BigDecimal.valueOf(500));
    PaymentSummaryDto result = paymentService.getTotalForUser(10L, Instant.now(), Instant.now());
    assertThat(result.totalAmount()).isEqualTo(BigDecimal.valueOf(500));
  }

  @Test
  void getTotalForAllUsers() {
    when(paymentRepositoryCustom.getTotalAmountForAllUsers(any(), any()))
            .thenReturn(BigDecimal.valueOf(900));
    PaymentSummaryDto result = paymentService.getTotalForAllUsers(Instant.now(), Instant.now());
    assertThat(result.totalAmount()).isEqualTo(BigDecimal.valueOf(900));
  }
}
package com.innowise.paymentservice.ourbox;

import com.innowise.paymentservice.kafka.PaymentProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

  private final PaymentOutboxRepository outboxRepository;
  private final PaymentProducer producer;

  @Scheduled(fixedDelay = 1000)
  public void processOutbox() {
    List<PaymentOutbox> pending = outboxRepository.findBySentFalse();
    for (PaymentOutbox entry : pending) {
      try {
        producer.sendPaymentEvent(entry.getEvent());
        entry.setSent(true);
        outboxRepository.save(entry);
      } catch (Exception e) {
        log.error("Failed to send outbox event {}", entry.getId(), e);
      }
    }
  }
}

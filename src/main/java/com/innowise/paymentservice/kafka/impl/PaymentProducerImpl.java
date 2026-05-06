package com.innowise.paymentservice.kafka.impl;

import com.innowise.paymentservice.kafka.PaymentProducer;
import com.innowise.paymentservice.kafka.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentProducerImpl implements PaymentProducer {

  private static final String TOPIC = "CREATE_PAYMENT";
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public PaymentProducerImpl(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendPaymentEvent(PaymentEvent event) {
    kafkaTemplate.executeInTransaction(kt -> {
      kt.send(TOPIC, event.orderId().toString(), event)
              .whenComplete((result, ex) -> {
                if (ex != null) {
                  log.error("Failed to send Kafka event for orderId={}", event.orderId(), ex);
                } else {
                  log.info("Kafka event sent successfully for orderId={}", event.orderId());
                }
              });
      return null;
    });
  }
}
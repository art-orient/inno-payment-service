package com.innowise.paymentservice.kafka.impl;

import com.innowise.paymentservice.kafka.PaymentProducer;
import com.innowise.paymentservice.kafka.PaymentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentProducerImpl implements PaymentProducer {

  private static final String TOPIC = "CREATE_PAYMENT";
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public PaymentProducerImpl(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendPaymentEvent(PaymentEvent event) {
    kafkaTemplate.send(TOPIC, String.valueOf(event.orderId()), event);
  }
}
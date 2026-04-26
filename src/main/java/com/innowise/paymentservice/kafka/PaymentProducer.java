package com.innowise.paymentservice.kafka;

public interface PaymentProducer {

  void sendPaymentEvent(PaymentEvent event);
}

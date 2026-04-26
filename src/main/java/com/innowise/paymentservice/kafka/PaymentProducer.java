package com.innowise.paymentservice.kafka;

import com.innowise.paymentservice.model.dto.PaymentEvent;

public interface PaymentProducer {

  void sendPaymentEvent(PaymentEvent event);
}

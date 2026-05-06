package com.innowise.paymentservice.ourbox;

import com.innowise.paymentservice.kafka.PaymentEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("payment_outbox")
public class PaymentOutbox {

  @Id
  private String id;

  private PaymentEvent event;

  private Instant createdAt;

  private boolean sent;
}

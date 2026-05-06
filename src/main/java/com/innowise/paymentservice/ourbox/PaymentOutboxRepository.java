package com.innowise.paymentservice.ourbox;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PaymentOutboxRepository extends MongoRepository<PaymentOutbox, String> {

  List<PaymentOutbox> findBySentFalse();
}

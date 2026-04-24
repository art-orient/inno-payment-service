package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.model.entity.PaymentDocument;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PaymentRepository extends MongoRepository<PaymentDocument, String>, PaymentCustomRepository {

  List<PaymentDocument> findByUserId(String userId);

  List<PaymentDocument> findByOrderId(String orderId);

  List<PaymentDocument> findByStatus(PaymentStatus status);
}

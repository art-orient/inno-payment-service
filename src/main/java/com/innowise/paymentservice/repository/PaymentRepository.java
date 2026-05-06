package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.model.entity.PaymentDocument;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository interface for accessing and managing payment documents stored in MongoDB.
 * <p>
 * Provides standard CRUD operations via {@link MongoRepository} and
 * domain-specific query methods for retrieving payments by key attributes.
 */
public interface PaymentRepository extends MongoRepository<PaymentDocument, String> {

  /**
   * Retrieves all payment documents associated with the specified user.
   *
   * @param userId the identifier of the user
   * @return a list of matching payment documents
   */
  List<PaymentDocument> findByUserId(Long userId);

  /**
   * Retrieves all payment documents associated with the specified order.
   *
   * @param orderId the identifier of the order
   * @return a list of matching payment documents
   */
  List<PaymentDocument> findByOrderId(Long orderId);

  /**
   * Retrieves all payment documents with the specified status.
   *
   * @param status the payment status to filter by
   * @return a list of matching payment documents
   */
  List<PaymentDocument> findByStatus(PaymentStatus status);
}

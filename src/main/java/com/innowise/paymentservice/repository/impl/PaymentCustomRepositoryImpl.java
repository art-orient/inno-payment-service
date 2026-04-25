package com.innowise.paymentservice.repository.impl;

import com.innowise.paymentservice.repository.PaymentCustomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class PaymentCustomRepositoryImpl implements PaymentCustomRepository {

  private final MongoTemplate mongoTemplate;

  @Override
  public BigDecimal getTotalAmountForUser(Long userId, Instant from, Instant to) {
    Criteria criteria = Criteria.where("userId").is(userId)
            .and("timestamp").gte(from).lte(to);
    return aggregateTotal(criteria);
  }

  @Override
  public BigDecimal getTotalAmountForAllUsers(Instant from, Instant to) {
    Criteria criteria = Criteria.where("timestamp").gte(from).lte(to);
    return aggregateTotal(criteria);
  }

  private BigDecimal aggregateTotal(Criteria criteria) {
    MatchOperation match = Aggregation.match(criteria);
    GroupOperation group = Aggregation.group().sum("paymentAmount").as("totalAmount");
    Aggregation aggregation = Aggregation.newAggregation(match, group);
    AggregationResults<TotalResult> result =
            mongoTemplate.aggregate(aggregation, "payments", TotalResult.class);
    TotalResult resultObj = result.getUniqueMappedResult();
    return resultObj != null ? resultObj.totalAmount : BigDecimal.ZERO;
  }

  private record TotalResult(BigDecimal totalAmount) {
  }
}

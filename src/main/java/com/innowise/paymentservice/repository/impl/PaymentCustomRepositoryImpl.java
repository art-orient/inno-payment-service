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
  public BigDecimal getTotalAmountForUser(String userId, Instant from, Instant to) {
    MatchOperation match = Aggregation.match(
            Criteria.where("userId").is(userId)
                    .and("timestamp").gte(from).lte(to)
    );

    GroupOperation group = Aggregation.group().sum("paymentAmount").as("total");

    Aggregation aggregation = Aggregation.newAggregation(match, group);

    AggregationResults<TotalResult> result =
            mongoTemplate.aggregate(aggregation, "payments", TotalResult.class);

    TotalResult total = result.getUniqueMappedResult();
    return total != null ? total.total : BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getTotalAmountForAllUsers(Instant from, Instant to) {
    MatchOperation match = Aggregation.match(
            Criteria.where("timestamp").gte(from).lte(to)
    );

    GroupOperation group = Aggregation.group().sum("paymentAmount").as("total");

    Aggregation aggregation = Aggregation.newAggregation(match, group);

    AggregationResults<TotalResult> result =
            mongoTemplate.aggregate(aggregation, "payments", TotalResult.class);

    TotalResult total = result.getUniqueMappedResult();
    return total != null ? total.total : BigDecimal.ZERO;
  }

  private static class TotalResult {
    BigDecimal total;
  }
}

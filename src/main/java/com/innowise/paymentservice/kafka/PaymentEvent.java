package com.innowise.paymentservice.kafka;

import com.innowise.paymentservice.model.entity.PaymentStatus;

import java.time.Instant;

public record PaymentEvent(
        Long orderId,
        String paymentId,
        PaymentStatus status,
        Instant timestamp
) {}

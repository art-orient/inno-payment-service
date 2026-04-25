package com.innowise.paymentservice.model.dto;

import com.innowise.paymentservice.model.entity.PaymentStatus;

public record PaymentFilterDto(
        Long userId,
        Long orderId,
        PaymentStatus status
) {}

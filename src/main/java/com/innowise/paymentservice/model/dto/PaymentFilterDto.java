package com.innowise.paymentservice.model.dto;

import com.innowise.paymentservice.model.entity.PaymentStatus;
import jakarta.annotation.Nullable;

public record PaymentFilterDto(
        @Nullable Long userId,
        @Nullable Long orderId,
        @Nullable PaymentStatus status) {
}

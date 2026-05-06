package com.innowise.paymentservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequestDto(

        @NotNull(message = "orderId must not be null")
        Long orderId,

        @NotNull(message = "userId must not be null")
        Long userId,

        @NotNull(message = "paymentAmount must not be null")
        @Positive(message = "paymentAmount must be positive")
        BigDecimal paymentAmount
) {}

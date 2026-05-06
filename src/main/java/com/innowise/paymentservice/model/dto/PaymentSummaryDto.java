package com.innowise.paymentservice.model.dto;

import java.math.BigDecimal;

public record PaymentSummaryDto(BigDecimal totalAmount) {
}

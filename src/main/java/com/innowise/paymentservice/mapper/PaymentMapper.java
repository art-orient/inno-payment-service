package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.model.dto.PaymentRequestDto;
import com.innowise.paymentservice.model.dto.PaymentResponseDto;
import com.innowise.paymentservice.model.entity.PaymentDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "timestamp", ignore = true)
  PaymentDocument toDocument(PaymentRequestDto dto);

  PaymentResponseDto toResponseDto(PaymentDocument document);
}
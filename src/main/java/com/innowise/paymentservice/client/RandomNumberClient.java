package com.innowise.paymentservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "random-number-client",
        url = "${app.external-api.random-number.base-url}")
public interface RandomNumberClient {

  @CircuitBreaker(name = "randomNumberBreaker", fallbackMethod = "getRandomNumberFallback")
  @RequestMapping("${app.external-api.random-number.full-path}")
  String getRandomNumberRaw();

  default Integer getOne() {
    String raw = getRandomNumberRaw();
    return Integer.parseInt(raw.trim());
  }

  default Integer getRandomNumberFallback(Exception ex) {
    return 1;
  }
}

package com.innowise.paymentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RandomNumberConfig {

  @Value("${app.random-api.url}")
  private String randomApiUrl;

  @Bean
  public WebClient randomNumberWebClient() {
    return WebClient.builder()
            .baseUrl(randomApiUrl)
            .build();
  }
}

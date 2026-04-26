package com.innowise.paymentservice.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.innowise.paymentservice.controller.PaymentController;
import com.innowise.paymentservice.kafka.PaymentEvent;
import com.innowise.paymentservice.model.dto.PaymentRequestDto;
import com.innowise.paymentservice.model.dto.PaymentResponseDto;
import com.innowise.paymentservice.model.entity.PaymentDocument;
import com.innowise.paymentservice.model.entity.PaymentStatus;
import com.innowise.paymentservice.repository.PaymentRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class PaymentIntegrationTest {

  private static final String PAYMENT_TOPIC = "CREATE_PAYMENT";
  private static final Duration CONSUMER_TIMEOUT = Duration.ofSeconds(10);

  @Container
  static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7.0");

  @Container
  static final KafkaContainer KAFKA_CONTAINER =
          new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

  @RegisterExtension
  static final WireMockExtension WIRE_MOCK =
          WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @Autowired
  private PaymentController controller;

  @Autowired
  private PaymentRepository paymentRepository;

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    registry.add("spring.kafka.producer.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    registry.add("app.external-api.random-number.base-url", WIRE_MOCK::baseUrl);
  }

  @BeforeEach
  void setUp() {
    paymentRepository.deleteAll();
    WIRE_MOCK.resetAll();
  }

  @Test
  void createPaymentPersistsSuccessAndPublishesKafkaEvent() {
    WIRE_MOCK.stubFor(get(urlPathEqualTo("/integers/")).willReturn(okJson("[42]")));
    PaymentResponseDto response = controller.createPayment(
            new PaymentRequestDto(22L, 1972L, new BigDecimal("22.03")));
    assertThat(response.status()).isEqualTo(PaymentStatus.SUCCESS);

    String paymentId = response.id();
    assertThat(response.status()).isEqualTo(PaymentStatus.SUCCESS);

    Optional<PaymentDocument> saved = paymentRepository.findById(paymentId);
    assertThat(saved).isPresent();
    PaymentDocument savedPayment = saved.orElseThrow();
    assertThat(savedPayment.getOrderId()).isEqualTo(22L);
    assertThat(savedPayment.getUserId()).isEqualTo(1972L);
    assertThat(savedPayment.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("22.03"));
    assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

    PaymentEvent event = consumeSinglePaymentEvent(22L);
    assertThat(event.orderId()).isEqualTo(22L);
    assertThat(event.paymentId()).isEqualTo(paymentId);
    assertThat(event.status()).isEqualTo(PaymentStatus.SUCCESS);
    assertThat(event.timestamp()).isNotNull();
  }

  @Test
  void createPaymentPersistsFailedWhenRandomNumberIsOdd() {
    WIRE_MOCK.stubFor(get(urlPathEqualTo("/integers/")).willReturn(okJson("[7]")));
    PaymentResponseDto createdPayment = controller.createPayment(
            new PaymentRequestDto(42L, 1975L, new BigDecimal("42.20")));
    String paymentId = createdPayment.id();
    assertThat(createdPayment.status()).isEqualTo(PaymentStatus.FAILED);

    PaymentDocument savedPayment = paymentRepository.findById(paymentId).orElseThrow();
    assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    assertThat(savedPayment.getOrderId()).isEqualTo(42L);
    assertThat(savedPayment.getUserId()).isEqualTo(1975L);
    assertThat(savedPayment.getPaymentAmount()).isEqualByComparingTo(new BigDecimal("42.20"));

    PaymentEvent event = consumeSinglePaymentEvent(42L);
    assertThat(event.paymentId()).isEqualTo(paymentId);
    assertThat(event.orderId()).isEqualTo(42L);
    assertThat(event.status()).isEqualTo(PaymentStatus.FAILED);
    assertThat(event.timestamp()).isNotNull();
  }

  private PaymentEvent consumeSinglePaymentEvent(Long orderId) {
    Map<String, Object> config = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers(),
            ConsumerConfig.GROUP_ID_CONFIG, "payment-test-" + UUID.randomUUID(),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class,
            JacksonJsonDeserializer.TRUSTED_PACKAGES, "*",
            JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, PaymentEvent.class.getName());
    try (KafkaConsumer<String, PaymentEvent> consumer = new KafkaConsumer<>(config)) {
      consumer.subscribe(List.of(PAYMENT_TOPIC));
      long deadline = System.currentTimeMillis() + CONSUMER_TIMEOUT.toMillis();
      while (System.currentTimeMillis() < deadline) {
        ConsumerRecords<String, PaymentEvent> records = consumer.poll(Duration.ofMillis(200));
        for (ConsumerRecord<String, PaymentEvent> record : records) {
          PaymentEvent event = record.value();
          if (event != null && orderId.equals(event.orderId())) {
            return event;
          }
        }
      }
    }
    throw new AssertionError("Kafka event not found for orderId=" + orderId);
  }
}
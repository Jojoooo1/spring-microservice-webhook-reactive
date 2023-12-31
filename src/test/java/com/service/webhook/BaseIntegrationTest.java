package com.service.webhook;

import java.nio.file.Paths;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.MountableFile;

@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

  public static final String RABBIT_USER_FROM_DEFINITION = "user";
  public static final String RABBIT_PASSWORD_FROM_DEFINITION = "password";

  @Container public static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.12.4");

  static {
    setRabbitConfig(rabbit);
    Startables.deepStart(rabbit).join();
  }

  @DynamicPropertySource
  static void applicationProperties(final DynamicPropertyRegistry registry) {
    registry.add("rabbitmq.host", rabbit::getHost);
    registry.add("rabbitmq.port", rabbit::getAmqpPort);
    registry.add("rabbitmq.username", () -> RABBIT_USER_FROM_DEFINITION);
    registry.add("rabbitmq.password", () -> RABBIT_PASSWORD_FROM_DEFINITION);
  }

  public static String random(final Integer... args) {
    return RandomStringUtils.randomAlphabetic(args.length == 0 ? 10 : args[0]);
  }

  public static String randomNumeric(final Integer... args) {
    return RandomStringUtils.randomNumeric(args.length == 0 ? 10 : args[0]);
  }

  private static String getResourcesDir() {
    return Paths.get("src", "test", "resources").toFile().getAbsolutePath();
  }

  private static String getRabbitDefinition() {
    return getResourcesDir() + "/testcontainers/rabbitmq-definition.json";
  }

  private static String getRabbitConfig() {
    return getResourcesDir() + "/testcontainers/rabbitmq.conf";
  }

  private static void setRabbitConfig(final RabbitMQContainer rabbit) {
    rabbit.withCopyFileToContainer(
        MountableFile.forHostPath(getRabbitDefinition()), "/etc/rabbitmq/definitions.json");
    rabbit.withCopyFileToContainer(
        MountableFile.forHostPath(getRabbitConfig()), "/etc/rabbitmq/rabbitmq.conf");
  }
}

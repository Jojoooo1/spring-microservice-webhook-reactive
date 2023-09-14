package com.service.webhook.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;

@Slf4j
@UtilityClass
public class RabbitMQUtils {

  public static final String RETRY_HEADER = "x-retry-count";
  public static final String DELAY_HEADER = "x-delay";
  public static final String URL_HEADER = "url";

  public static final Random random = new Random();

  public static String getUrl(final Message<LinkedHashMap<String, Object>> message) {
    return (String) message.getHeaders().get(URL_HEADER);
  }

  public static Map<String, Object> getHeaders(
      final Message<LinkedHashMap<String, Object>> message) {

    final HashMap<String, Object> headers = new HashMap<>();

    for (final Map.Entry<String, Object> entry : message.getHeaders().entrySet()) {
      if (entry.getValue() instanceof String) {
        headers.put(entry.getKey(), entry.getValue().toString());
      }
    }

    return headers;
  }

  public static Integer getRetryCount(final Map<String, Object> headers) {
    return headers.get(RETRY_HEADER) == null
        ? 0
        : Integer.parseInt(headers.get(RETRY_HEADER).toString());
  }

  public static Integer getRandom(final int min, final int max) {
    return random.ints(min, max).findFirst().getAsInt();
  }
}

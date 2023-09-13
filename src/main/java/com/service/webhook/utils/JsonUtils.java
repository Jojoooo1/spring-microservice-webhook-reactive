package com.service.webhook.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class JsonUtils {

  private static final String SERIALIZATION_ERROR_MESSAGE =
      "Something went wrong during serialization/deserialization";

  public static String serialize(final Object content) {
    try {
      return new ObjectMapper()
          .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
          .writeValueAsString(content);
    } catch (final Exception ex) {
      log.error(SERIALIZATION_ERROR_MESSAGE, ex);
      throw new IllegalArgumentException(ex);
    }
  }
}

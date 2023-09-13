package com.service.webhook.utils;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TraceUtils {

  public static String getTrace(final Tracer tracer) {
    final Span span = tracer.currentSpan();
    if (span != null) {
      return span.context().traceId();
    } else {
      return StringUtils.EMPTY;
    }
  }

  public static String getSpan(final Tracer tracer) {
    final Span span = tracer.currentSpan();
    if (span != null) {
      return span.context().spanId();
    } else {
      return StringUtils.EMPTY;
    }
  }
}

package com.service.webhook.rabbitmq.publishers;

import static com.service.webhook.constants.AppConstants.TRACE_ID_LOG;
import static java.lang.String.format;

import com.service.webhook.utils.JsonUtils;
import com.service.webhook.utils.RabbitMQUtils;
import com.service.webhook.utils.TraceUtils;
import io.micrometer.tracing.Tracer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * WebhookPublisher:
 *
 * <p>This publisher is used to retry webhook request in case of 4XXX. Rabbitmq will re deliver the
 * message with a delay using rabbitmq_delayed_message_exchange plugins
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookPublisher {

  @Value("${rabbitmq.publishers.webhook.exchange}")
  final String exchange;

  @Value("${rabbitmq.publishers.webhook.routingkey}")
  final String routingKey;

  @Value("${rabbitmq.publishers.webhook.queue}")
  final String queue;

  @Value("${rabbitmq.publishers.webhook.max-retry}")
  final Integer maxRetry;

  @Value("${rabbitmq.publishers.webhook.min-retry-delay}")
  final Integer minRetryDelay;

  @Value("${rabbitmq.publishers.webhook.max-retry-delay}")
  final Integer maxRetryDelay;

  private final AmqpTemplate amqpTemplate;
  private final Tracer tracer;

  public void publish(
      final String url, final Map<String, Object> headers, final Map<String, Object> payload) {

    try {

      final String msg = JsonUtils.serialize(payload);
      final Integer retryCount = RabbitMQUtils.getRetryCount(headers);

      if (retryCount > (this.maxRetry)) {
        log.info("[PUB][{}] retry exhausted message is being acknowledge.", this.queue);
        return;
      }

      final Integer newRetryCount = retryCount + 1;
      final Integer newDelay =
          RabbitMQUtils.getRandom(this.minRetryDelay, this.maxRetryDelay) * newRetryCount;

      this.processHeaders(headers, newRetryCount, newDelay);

      final MessageProperties props =
          MessagePropertiesBuilder.newInstance()
              .setContentType(MessageProperties.CONTENT_TYPE_JSON)
              .setContentEncoding(StandardCharsets.UTF_8.toString())
              .copyHeaders(headers)
              .build();

      log.info(
          "[PUB][{}] requeuing with retryCount[{}] and delay[{}ms]",
          this.queue,
          newRetryCount,
          newDelay);

      final Message message = MessageBuilder.withBody(msg.getBytes()).andProperties(props).build();

      this.amqpTemplate.convertAndSend(this.exchange, this.routingKey, message);
    } catch (final AmqpException ex) {
      log.warn(
          format(
              "[PUB][%s] error publishing message with url[%s] headers[%s] payload[%s]",
              this.queue, url, headers, payload),
          ex);
    }
  }

  private void processHeaders(
      final Map<String, Object> headers, final Integer newRetryCount, final Integer newDelay) {
    headers.put(RabbitMQUtils.RETRY_HEADER, Integer.toString(newRetryCount));
    headers.put(RabbitMQUtils.DELAY_HEADER, newDelay.toString());
    headers.put(TRACE_ID_LOG, TraceUtils.getTrace(this.tracer));
  }
}

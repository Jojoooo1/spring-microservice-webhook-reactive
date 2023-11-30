package com.service.webhook.rabbitmq.listeners;

import static java.lang.String.format;

import com.service.webhook.clients.WebhookHttpClient;
import com.service.webhook.rabbitmq.configs.RabbitConfig;
import com.service.webhook.utils.RabbitMQUtils;
import com.service.webhook.utils.UrlUtils;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookListener {

  public static final String WEBHOOK_LISTENER_ID = "WebhookListener";
  private final WebhookHttpClient httpClient;

  @RabbitListener(
      id = WEBHOOK_LISTENER_ID,
      containerFactory = RabbitConfig.RABBIT_WEBHOOK_LISTENER_FACTORY,
      queues = "${rabbitmq.listeners.webhook.queue}")
  public Mono<Void> process(final Message<LinkedHashMap<String, Object>> message) {
    try {

      final String url = RabbitMQUtils.getUrl(message);

      if (!UrlUtils.isValid(url)) {
        log.info("[ERROR] url[{}] is invalid, dropping message [{}]", url, message.getPayload());
        return Mono.empty();
      }

      return this.httpClient.sendWebhook(url, message);
    } catch (final Exception ex) {
      log.error(format("[ERROR] exiting with errorMessage: '%s'", ex.getMessage()), ex);
      return Mono.empty();
    }
  }
}

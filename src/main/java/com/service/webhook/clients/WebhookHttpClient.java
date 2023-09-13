package com.service.webhook.clients;

import static com.service.webhook.utils.WebClientUtils.getRootCauseErrorMessage;

import com.service.webhook.exceptions.WebhookRetriableException;
import com.service.webhook.rabbitmq.publishers.WebhookPublisher;
import com.service.webhook.utils.JsonUtils;
import com.service.webhook.utils.RabbitMQUtils;
import com.service.webhook.utils.WebClientUtils;
import io.netty.handler.ssl.SslClosedEngineException;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.PrematureCloseException;
import reactor.util.retry.Retry;

@Slf4j
@Component
public class WebhookHttpClient {

  private final WebClient webClient;
  private final WebhookPublisher webhookPublisher;

  @Autowired
  public WebhookHttpClient(
      @Value("${http.clients.default-timeout}") final Integer timeOut,
      final WebhookPublisher webhookPublisher) {
    this.webhookPublisher = webhookPublisher;
    this.webClient = WebClientUtils.createWebClient(timeOut, null);
  }

  public Mono<Void> sendWebhook(
      final String url, final Message<LinkedHashMap<String, Object>> message) {

    final String requestBody = JsonUtils.serialize(message.getPayload());
    final HashMap<String, Object> headers = RabbitMQUtils.getHeaders(message);
    final Integer retryCount = RabbitMQUtils.getRetryCount(headers);

    log.info("HTTP_REQUEST[webhook] retryCount[{}] url[{}] body[{}]", retryCount, url, requestBody);

    /*
     *
     * Important limitation:
     * For unknown reason Hooks.enableAutomaticContextPropagation() does not propagate the traces
     * downstream, so we are unable to follow retried message and success response logs by trace_id.
     * It was originally working with sleuth.
     * */
    return this.webClient
        .post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .headers(
            httpHeaders ->
                headers.forEach((k, v) -> httpHeaders.add(k, v != null ? v.toString() : null)))
        .bodyValue(requestBody)
        .exchangeToMono(this::defaultResponseHandler)
        .retryWhen(this.defaultRetryHandler())
        .onErrorResume(
            ex -> {
              log.warn(
                  "HTTP_ERROR[webhook] errorDetails['%s']".formatted(getRootCauseErrorMessage(ex)),
                  ex);
              return Mono.fromRunnable(
                      () -> this.webhookPublisher.publish(url, headers, message.getPayload()))
                  .subscribeOn(Schedulers.boundedElastic())
                  .then();
            });
  }

  private Mono<Void> defaultResponseHandler(final ClientResponse response) {
    final HttpStatusCode status = response.statusCode();

    return response
        // Does not enforce any class to keep http client generic.
        .bodyToMono(String.class)
        // Necessary to force mono execution on empty response.
        .defaultIfEmpty(StringUtils.EMPTY)
        .map(
            body -> {
              if (status.is2xxSuccessful()) {
                log.info("HTTP[RESPONSE] '{}'", body);
              } else {
                if (status.is4xxClientError()) {
                  log.info(
                      "HTTP_CLIENT_ERROR[webhook] skipping retry. status '{}' body '{}'",
                      status,
                      body);
                } else {
                  throw new WebhookRetriableException(status, body);
                }
              }
              return body;
            })
        .then();
  }

  /*
   * Error handler to improve network reliability.
   * */
  private Retry defaultRetryHandler() {
    return Retry.backoff(3, Duration.ofMillis(100))
        .filter(
            ex -> {
              if (ExceptionUtils.getRootCause(ex) instanceof PrematureCloseException) {
                log.info("HTTP[RETRY] PrematureCloseException detected retrying");
                return true;
              } else if (ExceptionUtils.getRootCause(ex) instanceof SslClosedEngineException) {
                log.info("HTTP[RETRY] SslClosedEngineException detected retrying");
                return true;
              }
              return false;
            });
  }
}
package com.service.webhook.exceptions;

import java.io.Serial;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class WebhookRetriableException extends RuntimeException {

  @Serial private static final long serialVersionUID = -3154618962130084535L;
  private final HttpStatusCode httpStatus;
  private final String errorMessage;

  public WebhookRetriableException(final HttpStatusCode httpStatus, final String errorMessage) {
    super("is5xxClientError detected, retrying with delay.");
    this.httpStatus = httpStatus;
    this.errorMessage = errorMessage;
  }
}

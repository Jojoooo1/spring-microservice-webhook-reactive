package com.service.webhook.controllers.actuator;

import static org.springframework.http.HttpStatus.OK;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This API is used to create a preStop hook for kubernetes (kubelet) to await a certain
 * delayInMillis before sending the SIGTERM signal
 */
@Slf4j
@Component
@ControllerEndpoint(id = "preStopHook")
class WebMvcPreStopHookEndpoint {

  @ResponseStatus(OK)
  @GetMapping("/{delayInMillis}")
  public void preStopHook(@PathVariable("delayInMillis") final long delayInMillis)
      throws InterruptedException {
    log.info("[preStopHook] received signal to sleep for {}ms", delayInMillis);
    Thread.sleep(delayInMillis);
  }
}

package com.service.webhook.rabbitmq.configs;

import static com.service.webhook.rabbitmq.listeners.WebhookListener.WEBHOOK_LISTENER_ID;

import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RabbitApplicationStartupListener
    implements ApplicationListener<ApplicationReadyEvent> {

  @Autowired RabbitListenerEndpointRegistry registry;

  /**
   * This event is executed as late as conceivably possible to indicate that the application is
   * ready to service requests.
   */
  @Override
  public void onApplicationEvent(@NonNull final ApplicationReadyEvent event) {
    this.registry.getListenerContainer(WEBHOOK_LISTENER_ID).start();
  }
}

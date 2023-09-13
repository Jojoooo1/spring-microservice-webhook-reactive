package com.service.webhook.rabbitmq.configs;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConverter {

  @Bean
  public Jackson2JsonMessageConverter jsonConverter() {
    return new JacksonMessageConverter();
  }

  public static class JacksonMessageConverter extends Jackson2JsonMessageConverter {

    public JacksonMessageConverter() {
      super();
    }

    @Override
    public Object fromMessage(final Message message) {
      message.getMessageProperties().setContentType("application/json");
      return super.fromMessage(message);
    }
  }
}

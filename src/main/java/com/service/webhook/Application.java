package com.service.webhook;

import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@Slf4j
@SpringBootApplication
public class Application {

  //  static {
  //    BlockHound.install();
  //  }

  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);

    log.info("[THREAD] Current threads on main group: {}", Thread.activeCount());
    log.info(
        "[THREAD] Current threads on all groups: {}",
        ManagementFactory.getThreadMXBean().getThreadCount());
  }

  @PostConstruct
  void started() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
    Hooks.enableAutomaticContextPropagation();
  }
}

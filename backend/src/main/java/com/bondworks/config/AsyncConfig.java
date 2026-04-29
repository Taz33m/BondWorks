package com.bondworks.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncConfig {
  @Bean(destroyMethod = "shutdown")
  ScheduledExecutorService quoteScheduler() {
    return Executors.newScheduledThreadPool(6);
  }
}

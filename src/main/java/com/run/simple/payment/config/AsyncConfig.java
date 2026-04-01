package com.run.simple.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executors;

/**
 * Configures @Async to run on Java 21 virtual threads. Virtual threads are ideal for I/O-bound work
 * like outbound HTTP webhook calls — they are cheap to create (no thread pool sizing needed) and
 * block without consuming OS threads.
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

  @Bean("virtualThreadExecutor")
  public AsyncTaskExecutor virtualThreadExecutor() {
    return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
  }

  @Override
  public AsyncTaskExecutor getAsyncExecutor() {
    return virtualThreadExecutor();
  }
}

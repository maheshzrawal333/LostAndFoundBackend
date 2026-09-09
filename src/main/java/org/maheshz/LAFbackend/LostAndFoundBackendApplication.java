package org.maheshz.LAFbackend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
public class LostAndFoundBackendApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(LostAndFoundBackendApplication.class, args);
    }

    // Resolves the thread pool conflict between WebSockets and Async Emails
    @Bean
    @Primary
    public TaskExecutor primaryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20); // Keep 20 threads ready
        executor.setMaxPoolSize(50);  // Allow up to 50 simultaneous email senders
        executor.setQueueCapacity(500); // Safely hold up to 500 emails in the waiting line
        executor.setThreadNamePrefix("PrimaryAsync-");
        executor.initialize();
        return executor;
    }
}
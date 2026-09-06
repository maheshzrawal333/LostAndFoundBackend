package org.maheshz.LAFbackend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class LostAndFoundBackendApplication {

    // 1. Force the entire backend to operate in Universal Time (UTC)
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(LostAndFoundBackendApplication.class, args);
    }
}
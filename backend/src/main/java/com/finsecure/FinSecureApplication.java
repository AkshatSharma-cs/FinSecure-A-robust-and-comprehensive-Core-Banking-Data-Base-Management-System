package com.finsecure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinSecureApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinSecureApplication.class, args);
    }
}

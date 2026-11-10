package com.elms.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ElmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElmsApplication.class, args);
    }
}

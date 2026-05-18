package com.hupms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class HupmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(HupmsApplication.class, args);
    }
}

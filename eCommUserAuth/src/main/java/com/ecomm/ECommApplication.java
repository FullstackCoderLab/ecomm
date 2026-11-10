package com.ecomm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main entry point for the eCommUserAuth Spring Boot application.
 * Registered with eCommAPIGateway via Eureka Service Discovery.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ECommApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommApplication.class, args);
    }
}

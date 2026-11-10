package com.ecomm.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.core.env.Environment;

/**
 * Main Entry Point for eCommAPIGateway.
 * 
 * Capabilities:
 * 1. Netflix Eureka Server: Service Registry & Discovery for microservices on port 8761.
 * 2. API Gateway: Unified entry point with path-based routing (/api/v1/auth/**, /api/v1/users/**).
 * 3. Client-Side Load Balancer: Round-Robin load distribution across healthy microservice instances.
 */
@SpringBootApplication
@EnableEurekaServer
@ConfigurationPropertiesScan
public class ECommApiGatewayApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ECommApiGatewayApplication.class);

    private final Environment env;

    public ECommApiGatewayApplication(Environment env) {
        this.env = env;
    }

    public static void main(String[] args) {
        SpringApplication.run(ECommApiGatewayApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String port = env.getProperty("server.port", "8761");
        String appName = env.getProperty("spring.application.name", "eCommAPIGateway");

        log.info("==========================================================================");
        log.info("Application '{}' is running!", appName);
        log.info("Eureka Server Dashboard:      http://localhost:{}/", port);
        log.info("Eureka Registration Endpoint: http://localhost:{}/eureka/", port);
        log.info("API Gateway Routes Info:      http://localhost:{}/gateway/routes", port);
        log.info("Registered Instances Info:    http://localhost:{}/gateway/instances", port);
        log.info("User Auth Route:              http://localhost:{}/api/v1/auth/** -> lb://eCommUserAuth", port);
        log.info("User Profile Route:           http://localhost:{}/api/v1/users/** -> lb://eCommUserAuth", port);
        log.info("Actuator Health Endpoint:     http://localhost:{}/actuator/health", port);
        log.info("==========================================================================");
    }
}

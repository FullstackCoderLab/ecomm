package com.ecomm.gateway.controller;

import com.ecomm.gateway.config.GatewayProperties;
import com.ecomm.gateway.loadbalancer.LoadBalancerService;
import com.ecomm.gateway.loadbalancer.ServiceInstanceInfo;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.*;

/**
 * Gateway Introspection & Monitoring Controller.
 * Provides endpoints to view active routes, discovered service instances, and health status.
 */
@RestController
@RequestMapping("/gateway")
public class GatewayDashboardController {

    private final LoadBalancerService loadBalancerService;
    private final GatewayProperties gatewayProperties;
    private final Environment env;

    public GatewayDashboardController(LoadBalancerService loadBalancerService,
                                      GatewayProperties gatewayProperties,
                                      Environment env) {
        this.loadBalancerService = loadBalancerService;
        this.gatewayProperties = gatewayProperties;
        this.env = env;
    }

    /**
     * Get list of all routes configured in the API Gateway.
     */
    @GetMapping("/routes")
    public ResponseEntity<Map<String, Object>> getRoutes() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("gatewayName", env.getProperty("spring.application.name", "eCommAPIGateway"));
        response.put("loadBalancerStrategy", gatewayProperties.getLoadbalancer().getStrategy());

        List<Map<String, String>> routes = List.of(
                Map.of(
                        "id", "auth-service-route",
                        "pathPattern", "/api/v1/auth/** or /api/auth/**",
                        "targetService", gatewayProperties.getServices().getAuthServiceId(),
                        "description", "User registration, login, and token generation"
                ),
                Map.of(
                        "id", "user-service-route",
                        "pathPattern", "/api/v1/users/** or /api/users/**",
                        "targetService", gatewayProperties.getServices().getAuthServiceId(),
                        "description", "User profile and user management APIs"
                ),
                Map.of(
                        "id", "service-prefix-route",
                        "pathPattern", "/ecommuserauth/**",
                        "targetService", gatewayProperties.getServices().getAuthServiceId(),
                        "description", "Explicit service-prefixed routing"
                ),
                Map.of(
                        "id", "dynamic-service-route",
                        "pathPattern", "/services/{serviceId}/**",
                        "targetService", "Dynamic (Discovered via Eureka)",
                        "description", "Dynamic routing to any service registered in Eureka"
                )
        );

        response.put("routes", routes);
        return ResponseEntity.ok(response);
    }

    /**
     * Get real-time status of all microservices and instances registered in Eureka.
     */
    @GetMapping("/instances")
    public ResponseEntity<Map<String, Object>> getInstances() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());

        Map<String, List<ServiceInstanceInfo>> services = loadBalancerService.getAllRegisteredServices();
        response.put("totalServices", services.size());
        response.put("services", services);

        return ResponseEntity.ok(response);
    }

    /**
     * Gateway and Eureka status overview.
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", "eCommAPIGateway");
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("description", "E-Commerce API Gateway, Eureka Registry & Load Balancer");
        response.put("port", env.getProperty("server.port", "8761"));
        response.put("eurekaDashboard", "http://localhost:" + env.getProperty("server.port", "8761") + "/");
        response.put("loadBalancerStrategy", gatewayProperties.getLoadbalancer().getStrategy());
        response.put("uptime", Instant.now().toString());

        return ResponseEntity.ok(response);
    }
}

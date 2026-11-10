package com.ecomm.gateway.controller;

import com.ecomm.gateway.config.GatewayProperties;
import com.ecomm.gateway.filter.GatewayLoggingFilter;
import com.ecomm.gateway.loadbalancer.LoadBalancerService;
import com.ecomm.gateway.loadbalancer.ServiceInstanceInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.*;

/**
 * Core API Gateway Reverse Proxy & Load Balancer Controller.
 * 
 * Handles incoming API requests, routes them to downstream microservices,
 * and balances load across multiple instances registered in Eureka.
 */
@RestController
public class GatewayProxyController {

    private static final Logger log = LoggerFactory.getLogger(GatewayProxyController.class);

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailer", "transfer-encoding", "upgrade", "host"
    );

    private final LoadBalancerService loadBalancerService;
    private final RestTemplate restTemplate;
    private final GatewayProperties gatewayProperties;

    public GatewayProxyController(LoadBalancerService loadBalancerService,
                                  RestTemplate gatewayRestTemplate,
                                  GatewayProperties gatewayProperties) {
        this.loadBalancerService = loadBalancerService;
        this.restTemplate = gatewayRestTemplate;
        this.gatewayProperties = gatewayProperties;
    }

    /**
     * Route user authentication endpoints directly to eCommUserAuth service.
     * Supports both versioned (/api/v1/auth/**) and standard (/api/auth/**) paths.
     * Examples:
     * - POST /api/v1/auth/register
     * - POST /api/v1/auth/login
     */
    @RequestMapping(value = {"/api/v1/auth/**", "/api/auth/**"}, method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS
    })
    public ResponseEntity<?> routeAuthService(HttpServletRequest request) throws IOException {
        String serviceId = gatewayProperties.getServices().getAuthServiceId();
        String targetPath = request.getRequestURI();
        return forwardRequest(serviceId, targetPath, request);
    }

    /**
     * Route user management endpoints directly to eCommUserAuth service.
     * Supports both versioned (/api/v1/users/**) and standard (/api/users/**) paths.
     * Examples:
     * - GET /api/v1/users/me
     * - GET /api/v1/users/all
     */
    @RequestMapping(value = {"/api/v1/users/**", "/api/users/**"}, method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS
    })
    public ResponseEntity<?> routeUsersService(HttpServletRequest request) throws IOException {
        String serviceId = gatewayProperties.getServices().getAuthServiceId();
        String targetPath = request.getRequestURI();
        return forwardRequest(serviceId, targetPath, request);
    }

    /**
     * Service-prefix routing for eCommUserAuth.
     * Strips "/ecommuserauth" and forwards remaining path to eCommUserAuth.
     * Example:
     * - POST /ecommuserauth/api/v1/auth/login -> forwards to /api/v1/auth/login
     */
    @RequestMapping(value = "/ecommuserauth/**", method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS
    })
    public ResponseEntity<?> routeEcommUserAuthPrefix(HttpServletRequest request) throws IOException {
        String serviceId = gatewayProperties.getServices().getAuthServiceId();
        String uri = request.getRequestURI();
        String targetPath = uri.substring("/ecommuserauth".length());
        if (targetPath.isEmpty()) {
            targetPath = "/";
        }
        return forwardRequest(serviceId, targetPath, request);
    }

    /**
     * Dynamic service discovery routing for ANY service registered with Eureka.
     * Format: /services/{serviceId}/**
     * Example:
     * - GET /services/eCommUserAuth/api/v1/users/me -> forwards to eCommUserAuth
     */
    @RequestMapping(value = "/services/{serviceId}/**", method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS
    })
    public ResponseEntity<?> routeDynamicService(@PathVariable String serviceId,
                                                 HttpServletRequest request) throws IOException {
        String uri = request.getRequestURI();
        String prefix = "/services/" + serviceId;
        String targetPath = uri.substring(prefix.length());
        if (targetPath.isEmpty()) {
            targetPath = "/";
        }
        return forwardRequest(serviceId, targetPath, request);
    }

    /**
     * Forwards an incoming HTTP request to a load-balanced instance of the target service.
     */
    private ResponseEntity<?> forwardRequest(String serviceId, String targetPath,
                                            HttpServletRequest request) throws IOException {

        // 1. Ask Load Balancer to pick a healthy instance from Eureka registry
        Optional<ServiceInstanceInfo> chosenInstance = loadBalancerService.choose(serviceId);

        if (chosenInstance.isEmpty()) {
            return buildServiceUnavailableResponse(serviceId, request);
        }

        ServiceInstanceInfo instance = chosenInstance.get();

        // 2. Build target URI
        String queryString = request.getQueryString();
        String targetUrl = instance.getBaseUrl() + targetPath + (queryString != null ? "?" + queryString : "");
        URI destinationUri = URI.create(targetUrl);

        log.debug("Gateway Proxy: Forwarding {} {} -> {}", request.getMethod(), request.getRequestURI(), targetUrl);

        // 3. Prepare headers
        HttpHeaders headers = copyHeaders(request);
        headers.set("X-Forwarded-Host", request.getServerName() + ":" + request.getServerPort());
        headers.set("X-Forwarded-Proto", request.getScheme());
        headers.set("X-Forwarded-Port", String.valueOf(request.getServerPort()));
        headers.set("X-Gateway-Service-Id", serviceId);

        // 4. Read body
        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body.length > 0 ? body : null, headers);

        // 5. Execute downstream call
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(destinationUri, method, httpEntity, byte[].class);

            // 6. Build response with load-balanced instance tracking headers
            HttpHeaders responseHeaders = new HttpHeaders();
            for (Map.Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
                if (!HOP_BY_HOP_HEADERS.contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                    responseHeaders.put(entry.getKey(), entry.getValue());
                }
            }

            // Expose which instance served this request (vital for testing load balancing)
            responseHeaders.set("X-LoadBalanced-Instance", instance.getBaseUrl());
            responseHeaders.set("X-LoadBalanced-Instance-Id", instance.getInstanceId());
            responseHeaders.set("X-LoadBalanced-Service", instance.getServiceId());

            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());

        } catch (ResourceAccessException ex) {
            log.error("Gateway Proxy: Connection failed to instance {} (serviceId={}): {}",
                    instance.getBaseUrl(), serviceId, ex.getMessage());

            Map<String, Object> errorBody = new LinkedHashMap<>();
            errorBody.put("timestamp", Instant.now().toString());
            errorBody.put("status", HttpStatus.BAD_GATEWAY.value());
            errorBody.put("error", "Bad Gateway");
            errorBody.put("message", "Failed to connect to downstream instance " + instance.getBaseUrl() + ": " + ex.getMessage());
            errorBody.put("serviceId", serviceId);
            errorBody.put("targetUrl", targetUrl);
            errorBody.put("path", request.getRequestURI());

            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorBody);
        }
    }

    private HttpHeaders copyHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                    Enumeration<String> values = request.getHeaders(name);
                    while (values.hasMoreElements()) {
                        headers.add(name, values.nextElement());
                    }
                }
            }
        }
        return headers;
    }

    private ResponseEntity<Map<String, Object>> buildServiceUnavailableResponse(String serviceId,
                                                                                HttpServletRequest request) {
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", Instant.now().toString());
        errorBody.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        errorBody.put("error", "Service Unavailable");
        errorBody.put("message", "No healthy instances available for service: '" + serviceId +
                "'. Please ensure '" + serviceId + "' is running and registered with Eureka Server.");
        errorBody.put("serviceId", serviceId);
        errorBody.put("path", request.getRequestURI());
        errorBody.put("eurekaDashboard", "http://localhost:" + request.getServerPort() + "/");
        errorBody.put("gatewayInstancesEndpoint", "http://localhost:" + request.getServerPort() + "/gateway/instances");

        String correlationId = request.getHeader(GatewayLoggingFilter.CORRELATION_ID_HEADER);
        if (correlationId != null) {
            errorBody.put("correlationId", correlationId);
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorBody);
    }
}

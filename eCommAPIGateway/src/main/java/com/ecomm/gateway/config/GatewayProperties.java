package com.ecomm.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for API Gateway and Load Balancer.
 */
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    private Services services = new Services();
    private Loadbalancer loadbalancer = new Loadbalancer();
    private Http http = new Http();

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

    public Loadbalancer getLoadbalancer() {
        return loadbalancer;
    }

    public void setLoadbalancer(Loadbalancer loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public Http getHttp() {
        return http;
    }

    public void setHttp(Http http) {
        this.http = http;
    }

    public static class Services {
        private String authServiceId = "eCommUserAuth";

        public String getAuthServiceId() {
            return authServiceId;
        }

        public void setAuthServiceId(String authServiceId) {
            this.authServiceId = authServiceId;
        }
    }

    public static class Loadbalancer {
        private String strategy = "round-robin";

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }
    }

    public static class Http {
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 15000;

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }
    }
}

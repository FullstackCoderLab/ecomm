package com.ecomm.gateway.loadbalancer;

import java.io.Serializable;
import java.util.Map;

/**
 * Representation of a microservice instance registered in Eureka.
 */
public class ServiceInstanceInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String instanceId;
    private String serviceId;
    private String host;
    private int port;
    private String status;
    private String homePageUrl;
    private String healthCheckUrl;
    private Map<String, String> metadata;

    public ServiceInstanceInfo() {
    }

    public ServiceInstanceInfo(String instanceId, String serviceId, String host, int port,
                               String status, String homePageUrl, String healthCheckUrl,
                               Map<String, String> metadata) {
        this.instanceId = instanceId;
        this.serviceId = serviceId;
        this.host = host;
        this.port = port;
        this.status = status;
        this.homePageUrl = homePageUrl;
        this.healthCheckUrl = healthCheckUrl;
        this.metadata = metadata;
    }

    public String getBaseUrl() {
        return "http://" + host + ":" + port;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHomePageUrl() {
        return homePageUrl;
    }

    public void setHomePageUrl(String homePageUrl) {
        this.homePageUrl = homePageUrl;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "ServiceInstanceInfo{" +
                "instanceId='" + instanceId + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", status='" + status + '\'' +
                '}';
    }
}

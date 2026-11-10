package com.ecomm.gateway.loadbalancer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service Contract for Client-Side Load Balancing and Eureka Discovery.
 */
public interface LoadBalancerService {

    /**
     * Choose a healthy instance for the specified service ID using the configured
     * load balancing strategy (e.g. Round-Robin).
     *
     * @param serviceId name of the service registered in Eureka (case-insensitive)
     * @return Optional containing the chosen ServiceInstanceInfo if available
     */
    Optional<ServiceInstanceInfo> choose(String serviceId);

    /**
     * Get all active and UP instances registered under the given service ID.
     *
     * @param serviceId name of the service registered in Eureka
     * @return list of healthy service instances
     */
    List<ServiceInstanceInfo> getHealthyInstances(String serviceId);

    /**
     * Retrieve all registered services and their current instances from Eureka.
     *
     * @return Map of service name to list of instance infos
     */
    Map<String, List<ServiceInstanceInfo>> getAllRegisteredServices();
}

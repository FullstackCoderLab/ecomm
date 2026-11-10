package com.ecomm.gateway.loadbalancer;

import com.ecomm.gateway.config.GatewayProperties;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Production-ready Eureka Service Registry reader and Client-Side Load Balancer.
 * 
 * Supports:
 * - Round-Robin Load Balancing across multiple instances of registered services.
 * - Random Load Balancing.
 * - Health-status filtering (only forwards to InstanceStatus.UP).
 * - Lazy registry lookup via ApplicationContext and EurekaServerContextHolder to guarantee
 *   zero bean lifecycle conflicts during Spring Boot startup.
 */
@Service
public class EurekaLoadBalancerServiceImpl implements LoadBalancerService {

    private static final Logger log = LoggerFactory.getLogger(EurekaLoadBalancerServiceImpl.class);

    private final ApplicationContext applicationContext;
    private final GatewayProperties gatewayProperties;
    private final ConcurrentHashMap<String, AtomicInteger> roundRobinCounters = new ConcurrentHashMap<>();

    public EurekaLoadBalancerServiceImpl(ApplicationContext applicationContext,
                                         GatewayProperties gatewayProperties) {
        this.applicationContext = applicationContext;
        this.gatewayProperties = gatewayProperties;
    }

    /**
     * Lazily and safely retrieves the Eureka PeerAwareInstanceRegistry.
     * Prevents premature initialization during Spring Boot application startup.
     */
    private PeerAwareInstanceRegistry getRegistry() {
        try {
            if (applicationContext != null && applicationContext.containsBean("peerAwareInstanceRegistry")) {
                return applicationContext.getBean(PeerAwareInstanceRegistry.class);
            }
        } catch (Exception ignored) {
        }
        try {
            if (EurekaServerContextHolder.getInstance() != null &&
                EurekaServerContextHolder.getInstance().getServerContext() != null) {
                return EurekaServerContextHolder.getInstance().getServerContext().getRegistry();
            }
        } catch (Throwable t) {
            log.trace("EurekaServerContextHolder not ready: {}", t.getMessage());
        }
        return null;
    }

    @Override
    public Optional<ServiceInstanceInfo> choose(String serviceId) {
        List<ServiceInstanceInfo> instances = getHealthyInstances(serviceId);

        if (instances.isEmpty()) {
            log.warn("LoadBalancer: No healthy instances found for serviceId='{}' in Eureka registry", serviceId);
            return Optional.empty();
        }

        if (instances.size() == 1) {
            ServiceInstanceInfo single = instances.get(0);
            log.debug("LoadBalancer: Single instance available for '{}': {}", serviceId, single.getBaseUrl());
            return Optional.of(single);
        }

        String strategy = gatewayProperties.getLoadbalancer().getStrategy();
        ServiceInstanceInfo selected;

        if ("random".equalsIgnoreCase(strategy)) {
            int index = ThreadLocalRandom.current().nextInt(instances.size());
            selected = instances.get(index);
            log.info("LoadBalancer [Random]: Routed '{}' to instance [{}/{}] -> {}",
                    serviceId, index + 1, instances.size(), selected.getBaseUrl());
        } else {
            // Default: Round-Robin
            String key = serviceId.trim().toUpperCase(Locale.ROOT);
            AtomicInteger counter = roundRobinCounters.computeIfAbsent(key, k -> new AtomicInteger(0));
            int nextVal = counter.getAndIncrement();
            // Handle integer overflow gracefully
            if (nextVal < 0) {
                counter.set(0);
                nextVal = 0;
            }
            int index = nextVal % instances.size();
            selected = instances.get(index);
            log.info("LoadBalancer [Round-Robin]: Routed '{}' to instance [{}/{}] (instanceId={}) -> {}",
                    serviceId, index + 1, instances.size(), selected.getInstanceId(), selected.getBaseUrl());
        }

        return Optional.of(selected);
    }

    @Override
    public List<ServiceInstanceInfo> getHealthyInstances(String serviceId) {
        if (serviceId == null || serviceId.isBlank()) {
            return Collections.emptyList();
        }

        PeerAwareInstanceRegistry reg = getRegistry();
        if (reg == null) {
            log.warn("LoadBalancer: PeerAwareInstanceRegistry is not yet available");
            return Collections.emptyList();
        }

        String lookupKey = serviceId.trim().toUpperCase(Locale.ROOT);
        Application application = reg.getApplication(lookupKey);

        if (application == null || application.getInstances() == null) {
            return Collections.emptyList();
        }

        return application.getInstances().stream()
                .filter(inst -> inst.getStatus() == InstanceInfo.InstanceStatus.UP)
                .map(this::mapToInfo)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<ServiceInstanceInfo>> getAllRegisteredServices() {
        Map<String, List<ServiceInstanceInfo>> result = new LinkedHashMap<>();
        PeerAwareInstanceRegistry reg = getRegistry();

        if (reg == null || reg.getApplications() == null) {
            return result;
        }

        List<Application> apps = reg.getApplications().getRegisteredApplications();
        if (apps != null) {
            for (Application app : apps) {
                List<ServiceInstanceInfo> instances = app.getInstances().stream()
                        .map(this::mapToInfo)
                        .collect(Collectors.toList());
                result.put(app.getName(), instances);
            }
        }

        return result;
    }

    private ServiceInstanceInfo mapToInfo(InstanceInfo info) {
        String host = (info.getIPAddr() != null && !info.getIPAddr().isBlank()) ? info.getIPAddr() : info.getHostName();
        return new ServiceInstanceInfo(
                info.getId(),
                info.getAppName(),
                host,
                info.getPort(),
                info.getStatus() != null ? info.getStatus().name() : "UNKNOWN",
                info.getHomePageUrl(),
                info.getHealthCheckUrl(),
                info.getMetadata()
        );
    }
}

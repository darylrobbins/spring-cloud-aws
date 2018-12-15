package org.springframework.cloud.aws.cloudmap.discovery;

import com.amazonaws.services.servicediscovery.AWSServiceDiscoveryAsync;
import com.amazonaws.services.servicediscovery.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.cloudmap.CloudMapServiceInstance;
import org.springframework.cloud.aws.cloudmap.serviceregistry.CloudMapRegistration;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;
import java.util.stream.Collectors;

/** Cloud Map Service Discovery Client */
public class CloudMapDiscoveryClient implements DiscoveryClient {

    public static final String DESCRIPTION = "AWS Cloud Map Discovery Client";

    private static final String AWS_INSTANCE_PORT = "AWS_INSTANCE_PORT";
    private static final String SECURE = "SECURE";
    private static final String CONTEXT_PATH = "CONTEXT_PATH";
    private static final String AWS_INSTANCE_CNAME = "AWS_INSTANCE_CNAME";
    private static final String AWS_INSTANCE_IPV4 = "AWS_INSTANCE_IPV4";
    private static final String AWS_INSTANCE_IPV6 = "AWS_INSTANCE_IPV6";

    private final AWSServiceDiscoveryAsync client;

    @Value("${aws.cloudmap.service.namespace}")
    private String namespace = null;

    public CloudMapDiscoveryClient(AWSServiceDiscoveryAsync client) {
        this.client = client;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        final DiscoverInstancesResult discoverInstancesResult = client.discoverInstances(
                new DiscoverInstancesRequest()
                    .withNamespaceName(namespace)
                    .withServiceName(serviceId)
                    .withHealthStatus(HealthStatusFilter.HEALTHY));

        return discoverInstancesResult.getInstances().stream()
                .map(instance -> {
                    CloudMapServiceInstance.Builder builder =
                            CloudMapServiceInstance.builder()
                                .withServiceId(instance.getServiceName())
                                .withInstanceId(instance.getInstanceId())
                                .withNamespace(instance.getNamespaceName());

                    if (instance.getAttributes().containsKey(AWS_INSTANCE_PORT)) {
                        builder.withPort(Integer.parseInt(instance.getAttributes().get(AWS_INSTANCE_PORT)));
                    }

                    builder.withMetadata(instance.getAttributes());

                    if (instance.getAttributes().containsKey(SECURE)) {
                        builder.withSecure(Boolean.parseBoolean(instance.getAttributes().get(SECURE)));
                    }

                    if (instance.getAttributes().containsKey(CONTEXT_PATH)) {
                        builder.withContextPath(instance.getAttributes().get(CONTEXT_PATH));
                    }

                    if (instance.getAttributes().containsKey(AWS_INSTANCE_CNAME)) {
                        builder.withHost(instance.getAttributes().get(AWS_INSTANCE_CNAME));
                    } else if (instance.getAttributes().containsKey(AWS_INSTANCE_IPV4)) {
                        builder.withHost(instance.getAttributes().get(AWS_INSTANCE_IPV4));
                    } else if (instance.getAttributes().containsKey(AWS_INSTANCE_IPV6)) {
                        builder.withHost(instance.getAttributes().get(AWS_INSTANCE_IPV6));
                    }

                    return builder.build();

                }).collect(Collectors.toList());
    }

    @Override
    public List<String> getServices() {
        final ListServicesResult listServicesResult = client.listServices(
                new ListServicesRequest().withFilters(
                        new ServiceFilter()
                                .withName(ServiceFilterName.NAMESPACE_ID)
                                .withCondition(FilterCondition.EQ)
                                .withValues(namespace)));

        return listServicesResult.getServices().stream()
                .map(ServiceSummary::getName)
                .collect(Collectors.toList());
    }
}

/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.aws.cloudmap;

import com.amazonaws.services.servicediscovery.AWSServiceDiscovery;
import com.amazonaws.services.servicediscovery.AWSServiceDiscoveryAsync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.aws.cloudmap.discovery.CloudMapDiscoveryClient;
import org.springframework.cloud.aws.cloudmap.serviceregistry.CloudMapAutoServiceRegistration;
import org.springframework.cloud.aws.cloudmap.serviceregistry.CloudMapRegistration;
import org.springframework.cloud.aws.cloudmap.serviceregistry.CloudMapServiceRegistry;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.noop.NoopDiscoveryClientAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;

import java.util.Collections;
import java.util.Map;

import static org.springframework.cloud.commons.util.IdUtils.getDefaultInstanceId;

@Configuration
@EnableConfigurationProperties
@ConditionalOnClass(AWSServiceDiscoveryAsync.class)
@EnableAutoConfiguration
@ConditionalOnProperty(prefix=CloudMapProperties.CONFIG_PREFIX, value = "enabled", matchIfMissing = true)
@AutoConfigureBefore({ NoopDiscoveryClientAutoConfiguration.class,
        CommonsClientAutoConfiguration.class, ServiceRegistryAutoConfiguration.class,
        SimpleDiscoveryClientAutoConfiguration.class })
@AutoConfigureAfter(name = {"org.springframework.cloud.autoconfigure.RefreshAutoConfiguration",
        "org.springframework.cloud.aws.cloudmap.discovery.CloudMapDiscoveryClientConfiguration",
        "org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration"})
public class CloudMapAutoConfiguration {

//    @Bean
//    public HasFeatures eurekaFeature() {
//        return HasFeatures.namedFeature("CloudMap", EurekaClient.class);
//    }

    private final ConfigurableEnvironment env;
    private final InetUtils inetUtils;

    public CloudMapAutoConfiguration(ConfigurableEnvironment env, InetUtils inetUtils) {
        this.env = env;
        this.inetUtils = inetUtils;
    }

    @Bean
    @ConditionalOnBean(AutoServiceRegistrationProperties.class)
    @ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
    public CloudMapAutoServiceRegistration eurekaAutoServiceRegistration(ApplicationContext context, CloudMapServiceRegistry registry,
                                                                         CloudMapRegistration registration) {
        return new CloudMapAutoServiceRegistration(context, registry, registration);
    }

    @Bean
    public DiscoveryClient discoveryClient(AWSServiceDiscoveryAsync client) {
        return new CloudMapDiscoveryClient(client);
    }

    @Bean
    public CloudMapServiceRegistry cloudMapServiceRegistry(AWSServiceDiscoveryAsync client) {
        return new CloudMapServiceRegistry(client);
    }

    private String getProperty(String property) {
        return getProperty(property, "");
    }

    private String getProperty(String property, String defaultValue) {
        return this.env.containsProperty(property) ? this.env.getProperty(property) : defaultValue;
    }

    @Bean
	@ConditionalOnBean(AutoServiceRegistrationProperties.class)
	@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
	public CloudMapRegistration registration() {
        final String namespace = getProperty("aws.cloudmap.service.namespace");
        final String contextPath = getProperty("server.context-path", "/");
        final int serverPort = Integer.valueOf(env.getProperty("server.port", env.getProperty("port", "8080")));
        final String instanceId = getDefaultInstanceId(env);
        final String serviceName = getProperty("aws.cloudmap.service.name", getProperty("spring.application.name"));
        final boolean securePortEnabled = Boolean.valueOf(getProperty("aws.cloudmap.instance.securePortEnabled"));
        final Map<String, String> metadata = env.getProperty("aws.cloudmap.instance.metadata", Map.class, Collections.emptyMap());

        final boolean preferIpAddress = Boolean.valueOf(getProperty("aws.cloudmap.instance.preferIpAddress"));
        final String configuredHostname = getProperty("aws.cloudmap.instance.hostname");
        final String configuredIpAddress = getProperty("aws.cloudmap.instance.ipAddress");

        final String desiredHost;
        if (preferIpAddress && StringUtils.hasText(configuredIpAddress)) {
            desiredHost = configuredIpAddress;
        } else if (StringUtils.hasText(configuredHostname)) {
            desiredHost = configuredHostname;
        } else if (!preferIpAddress && StringUtils.hasText(configuredIpAddress)) {
            desiredHost = configuredIpAddress;
        } else {
            desiredHost = inetUtils.findFirstNonLoopbackAddress().getHostAddress();
        }

        CloudMapServiceInstance serviceInstance =
                CloudMapServiceInstance.builder()
                    .withInstanceId(instanceId)
                    .withServiceId(serviceName)
                    .withContextPath(contextPath)
                    .withHost(desiredHost)
                    .withNamespace(namespace)
                    .withPort(serverPort)
                    .withSecure(securePortEnabled)
                    .withMetadata(metadata)
                    .build();

        return new CloudMapRegistration(serviceInstance);
	}

}

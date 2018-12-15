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

package org.springframework.cloud.aws.cloudmap.serviceregistry;

import com.amazonaws.services.servicediscovery.AWSServiceDiscoveryAsync;
import com.amazonaws.services.servicediscovery.model.*;
import org.springframework.cloud.aws.cloudmap.CloudMapServiceInstance;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;
import static org.springframework.boot.actuate.health.Status.UNKNOWN;

public class CloudMapServiceRegistry implements ServiceRegistry<CloudMapRegistration> {

    private final AWSServiceDiscoveryAsync client;

    public CloudMapServiceRegistry(AWSServiceDiscoveryAsync client) {
        this.client = client;
    }

    @Override
    public void register(CloudMapRegistration registration) {
        final Future<RegisterInstanceResult> future =
                client.registerInstanceAsync(
                        new RegisterInstanceRequest()
                                .withServiceId(registration.getServiceId())
                                .withInstanceId(registration.getInstanceId()));

        try {
            future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register service instance", e);
        }
    }

    @Override
    public void deregister(CloudMapRegistration registration) {
        final Future<DeregisterInstanceResult> future =
                client.deregisterInstanceAsync(
                    new DeregisterInstanceRequest()
                            .withServiceId(registration.getServiceId())
                            .withInstanceId(registration.getInstanceId())
                );

        try {
            future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deregister service instance", e);
        }

    }

    @Override
    public void close() {
        client.shutdown();
    }

    @Override
    public void setStatus(CloudMapRegistration registration, String status) {
        throw new UnsupportedOperationException("CloudMap does not support setting an instance out of service");
    }

    @Override
    public Object getStatus(CloudMapRegistration registration) {
        final GetInstancesHealthStatusResult result =
                client.getInstancesHealthStatus(
                    new GetInstancesHealthStatusRequest()
                            .withServiceId(registration.getServiceId())
                            .withInstances(registration.getInstanceId())
                );

        switch (result.getStatus().get(registration.getInstanceId())) {
            case "HEALTHY":
                return UP.getCode();
            case "UNHEALTHY":
                return DOWN.getCode();
            default: // including UNKNOWN
                return UNKNOWN.getCode();
        }
    }
}

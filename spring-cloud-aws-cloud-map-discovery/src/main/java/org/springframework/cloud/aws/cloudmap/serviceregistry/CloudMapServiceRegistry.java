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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.cloudmap.CloudMapProperties;
import org.springframework.cloud.aws.cloudmap.CloudMapServiceInstance;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;
import static org.springframework.boot.actuate.health.Status.UNKNOWN;

public class CloudMapServiceRegistry implements ServiceRegistry<CloudMapRegistration> {

    private static final Log log = LogFactory.getLog(CloudMapServiceRegistry.class);

    private final AWSServiceDiscoveryAsync client;

    @Autowired
    private CloudMapProperties props = null;

    public CloudMapServiceRegistry(AWSServiceDiscoveryAsync client) {
        this.client = client;
    }

    @Override
    public void register(CloudMapRegistration registration) {

        // Create namespace if it doesn't already exist
        if (!namespaceExists(registration.getNamespace())) {

            if (!props.isCreateNamespace()) {
                throw new IllegalStateException("Namespace does not exist but configuration says not to create it");
            }

            Future<CreateHttpNamespaceResult> createNamespaceFuture =
                    client.createHttpNamespaceAsync(new CreateHttpNamespaceRequest().withName(registration.getNamespace()));

            while (!createNamespaceFuture.isDone()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted while creating namespace", e);
                }
            }

            if (createNamespaceFuture.isCancelled()) {
                throw new RuntimeException("Registration was cancelled");
            }

            log.info("Namespace created " + registration.getNamespace());
        }

        // Create service if it doesn't already exist
        if (!serviceExists(registration.getServiceId())) {

            if (!props.isCreateService()) {
                throw new IllegalStateException("Service does not exist but configuration says not to create it");
            }

            try {
                client.createService(new CreateServiceRequest()
                        .withNamespaceId(registration.getNamespace())
                        .withName(registration.getServiceId()));
            } catch (InvalidInputException e) {
                throw new RuntimeException("Could not create service due to invalid input", e);
            } catch (ResourceLimitExceededException e) {
                throw new RuntimeException("Could not create service, as resource limited exceeded. You likely need " +
                        "to contact AWS support for a limit increased.", e);
            } catch (NamespaceNotFoundException e) {
                throw new RuntimeException("Could not create service, as namespace did not exist.", e);
            } catch (ServiceAlreadyExistsException e) {
                // ignore since we're already in the desired state
            }

            log.info("Service created " + registration.getServiceId());
        }

        // Register the service instance
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

    private boolean namespaceExists(String namespace) {
        try {
            client.getNamespace(new GetNamespaceRequest().withId(namespace));
        } catch (NamespaceNotFoundException e) {
            return false;
        } catch (InvalidInputException e) {
            log.warn("Invalid input when looking up namespace", e);
        }

        return true;
    }

    private boolean serviceExists(String service) {

        try {
            client.getService(new GetServiceRequest().withId(service));
        } catch (ServiceNotFoundException e) {
            return false;
        } catch (InvalidInputException e) {
            log.warn("Invalid input when looking up service", e);
        }

        return true;
    }
}

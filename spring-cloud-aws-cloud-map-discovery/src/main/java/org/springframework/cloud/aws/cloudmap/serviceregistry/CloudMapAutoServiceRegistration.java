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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Based on Eureka implementation
 */
public class CloudMapAutoServiceRegistration extends AbstractAutoServiceRegistration<CloudMapRegistration>
        implements AutoServiceRegistration, SmartLifecycle, Ordered {

    private static final Log log = LogFactory.getLog(CloudMapAutoServiceRegistration.class);

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final int order = 0;

    private final ApplicationContext context;

    private final CloudMapRegistration registration, managementRegistration;

    public CloudMapAutoServiceRegistration(ApplicationContext context, CloudMapServiceRegistry serviceRegistry,
                                           CloudMapRegistration registration, CloudMapRegistration managementRegistration) {
        super(serviceRegistry);
        this.context = context;
        this.registration = registration;
        this.managementRegistration = managementRegistration;
    }

    public CloudMapAutoServiceRegistration(ApplicationContext context, CloudMapServiceRegistry serviceRegistry,
                                           CloudMapRegistration registration) {
        this(context, serviceRegistry, registration, null);
    }

    @Override
    public void start() {

        // Only register if we have a port
        if (!this.running.get() && this.registration.getPort() > 0) {

            this.getServiceRegistry().register(this.registration);

            this.context.publishEvent(
                    new InstanceRegisteredEvent<>(this, this.registration));
            this.running.set(true);
        } else {
            log.warn("Service does not have a port assigned, so skipping registration");
        }

    }

    @Override
    protected Object getConfiguration() {
        return null;
    }

    @Override
    protected boolean isEnabled() {
        return false;
    }

    @Override
    protected CloudMapRegistration getRegistration() {
        return registration;
    }

    @Override
    protected CloudMapRegistration getManagementRegistration() {
        return managementRegistration;
    }

    @Override
    public void stop() {
        this.getServiceRegistry().deregister(this.registration);
        this.running.set(false);
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @EventListener
    public void handleWebServerInitializedEvent(WebServerInitializedEvent event) {
        start();
    }

    @EventListener
    public void handleContextClosedEvent(ContextClosedEvent event) {
        if( event.getApplicationContext() == context ) {
            stop();
        }
    }

}

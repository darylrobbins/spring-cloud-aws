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
import org.springframework.cloud.aws.cloudmap.CloudMapServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.net.URI;
import java.util.Map;

/** Representation of a CloudMap Service Instance */
public class CloudMapRegistration implements Registration {

    private final CloudMapServiceInstance serviceInstance;

    public CloudMapRegistration(CloudMapServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public CloudMapServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    @Override
    public String getServiceId() {
        return getServiceInstance().getServiceId();
    }

    @Override
    public String getHost() {
        return getServiceInstance().getHost();
    }

    public String getInstanceId() {
        return getServiceInstance().getInstanceId();
    }

    public int getPort() {
        return getServiceInstance().getPort();
    }

    @Override
    public boolean isSecure() {
        return getServiceInstance().isSecure();
    }

    @Override
    public URI getUri() {
        return CloudMapServiceInstance.getUri(serviceInstance);
    }

    public String getNamespace() {
        return getServiceInstance().getNamespace();
    }

    @Override
    public Map<String, String> getMetadata() {
        return getServiceInstance().getMetadata();
    }
}

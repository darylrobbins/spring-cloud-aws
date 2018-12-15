/*
 * Copyright 2013-2014 the original author or authors.
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

package org.springframework.cloud.aws.autoconfigure.cloudmap;

import com.amazonaws.services.servicediscovery.AWSServiceDiscoveryAsync;
import com.amazonaws.services.servicediscovery.AWSServiceDiscoveryAsyncClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.aws.cloudmap.CloudMapAutoConfiguration;
import org.springframework.cloud.aws.cloudmap.CloudMapProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cloud Bootstrap Configuration for setting up Cloud Map discovery and its
 * dependencies.
 *
 * @author Joris Kuipers
 * @since 2.0.0
 */
@Configuration
@EnableConfigurationProperties(CloudMapProperties.class)
@ConditionalOnClass({ AWSServiceDiscoveryAsync.class, CloudMapAutoConfiguration.class })
@ConditionalOnProperty(prefix = CloudMapProperties.CONFIG_PREFIX, name= "enabled", matchIfMissing = true)
public class AwsCloudMapBootstrapConfiguration {

    @Bean
    @ConditionalOnMissingBean
    AWSServiceDiscoveryAsync serviceDiscoveryClient() {
        return AWSServiceDiscoveryAsyncClientBuilder.defaultClient();
    }

}

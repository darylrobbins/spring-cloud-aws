package org.springframework.cloud.aws.cloudmap.discovery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.aws.cloudmap.CloudMapProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConditionalOnClass(CloudMapProperties.class)
@ConditionalOnProperty(prefix= CloudMapProperties.CONFIG_PREFIX, value = "enabled", matchIfMissing = true)
public class CloudMapDiscoveryClientConfiguration {

    class Marker {}

    @Bean
    public Marker cloudMapDiscoverClientMarker() {
        return new Marker();
    }

    // TODO Refresher
//    @Configuration
//    @ConditionalOnClass(RefreshScopeRefreshedEvent.class)
//    protected static class EurekaClientConfigurationRefresher implements ApplicationListener<RefreshScopeRefreshedEvent> {
//
//        @Autowired(required = false)
//        private EurekaClient eurekaClient;
//
//        @Autowired(required = false)
//        private EurekaAutoServiceRegistration autoRegistration;
//
//        public void onApplicationEvent(RefreshScopeRefreshedEvent event) {
//            //This will force the creation of the EurkaClient bean if not already created
//            //to make sure the client will be reregistered after a refresh event
//            if(eurekaClient != null) {
//                eurekaClient.getApplications();
//            }
//            if (autoRegistration != null) {
//                // register in case meta data changed
//                this.autoRegistration.stop();
//                this.autoRegistration.start();
//            }
//        }
//    }

    @Configuration
    @ConditionalOnClass(Health.class)
    protected static class EurekaHealthIndicatorConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnEnabledHealthIndicator("cloudmap")
        public CloudMapHealthIndicator cloudMapHealthIndicator() {
            return new CloudMapHealthIndicator();
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "aws.cloudmap.healthcheck.enabled", matchIfMissing = false)
    protected static class EurekaHealthCheckHandlerConfiguration {

        @Autowired(required = false)
        private HealthAggregator healthAggregator = new OrderedHealthAggregator();

        @Bean
        public CloudMapHealthCheckHandler cloudMapHealthCheckHandler() {
            return new CloudMapHealthCheckHandler(this.healthAggregator);
        }
    }
}

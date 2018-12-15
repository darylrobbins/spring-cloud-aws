package org.springframework.cloud.aws.cloudmap.discovery;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.health.CompositeHealthIndicator;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.client.discovery.health.DiscoveryCompositeHealthIndicator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

import java.util.Map;

public class CloudMapHealthCheckHandler implements ApplicationContextAware, InitializingBean {

    private final CompositeHealthIndicator healthIndicator;

    private ApplicationContext applicationContext;

    public CloudMapHealthCheckHandler(HealthAggregator healthAggregator) {
        Assert.notNull(healthAggregator, "HealthAggregator must not be null");
        this.healthIndicator = new CompositeHealthIndicator(healthAggregator);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Map<String, HealthIndicator> healthIndicators = applicationContext.getBeansOfType(HealthIndicator.class);

        for (Map.Entry<String, HealthIndicator> entry : healthIndicators.entrySet()) {

            //ignore EurekaHealthIndicator and flatten the rest of the composite
            //otherwise there is a never ending cycle of down. See gh-643
            if (entry.getValue() instanceof DiscoveryCompositeHealthIndicator) {
                DiscoveryCompositeHealthIndicator indicator = (DiscoveryCompositeHealthIndicator) entry.getValue();
                for (DiscoveryCompositeHealthIndicator.Holder holder : indicator.getHealthIndicators()) {
                    if (!(holder.getDelegate() instanceof CloudMapHealthIndicator)) {
                        healthIndicator.addHealthIndicator(holder.getDelegate().getName(), holder);
                    }
                }

            }
            else {
                healthIndicator.addHealthIndicator(entry.getKey(), entry.getValue());
            }
        }
    }

    protected CompositeHealthIndicator getHealthIndicator() {
        return healthIndicator;
    }

}

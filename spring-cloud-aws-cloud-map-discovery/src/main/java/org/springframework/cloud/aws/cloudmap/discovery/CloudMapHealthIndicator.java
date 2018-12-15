package org.springframework.cloud.aws.cloudmap.discovery;

import com.amazonaws.services.servicediscovery.AWSServiceDiscoveryAsync;
import com.amazonaws.services.servicediscovery.model.GetNamespaceRequest;
import com.amazonaws.services.servicediscovery.model.GetNamespaceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.client.discovery.health.DiscoveryHealthIndicator;

public class CloudMapHealthIndicator implements DiscoveryHealthIndicator {

    @Autowired
    private AWSServiceDiscoveryAsync client = null;

    @Value("aws.cloudmap.service.namespace")
    private String namespace = null;

    @Override
    public String getName() {
        return "cloudmap";
    }

    @Override
    public Health health() {
        if (client != null) {
            try {
                client.getNamespace(new GetNamespaceRequest().withId(this.namespace));
                return Health.up().build();
            } catch (Exception e) {
                return Health.down(e).build();
            }
        } else {
            return Health.unknown().build();
        }
    }
}

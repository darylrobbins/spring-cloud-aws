package org.springframework.cloud.aws.cloudmap;

import org.springframework.cloud.aws.cloudmap.serviceregistry.CloudMapRegistration;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class CloudMapServiceInstance extends DefaultServiceInstance {

    private final String namespace;
    private final String contextPath;
    private final String instanceId;

    public CloudMapServiceInstance(String instanceId, String serviceId, String host, int port, boolean secure,
                                   Map<String, String> metadata, String namespace, String contextPath) {
        super(serviceId, host, port, secure, metadata);
        this.instanceId = instanceId;
        this.namespace = namespace;
        this.contextPath = contextPath;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getContextPath() {
        return StringUtils.hasText(contextPath) ? contextPath : "/";
    }

    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Creates a URI from the given ServiceInstance's host:port and context path.
     * @param instance the CloudMapServiceInstance.
     * @return URI of the form (secure)?https:http + "host:port/content-path".
     */
    public static URI getUri(CloudMapServiceInstance instance) {
        String scheme = (instance.isSecure()) ? "https" : "http";
        String uri = String.format("%s://%s:%s%s", scheme, instance.getHost(),
                instance.getPort(), instance.getContextPath());
        return URI.create(uri);
    }

    @Override
    public String toString() {
        return "CloudMapServiceInstance{" +
                "instanceId='" + getInstanceId() + '\'' +
                ", serviceId='" + getServiceId() + '\'' +
                ", host='" + getHost() + '\'' +
                ", port=" + getPort() +
                ", secure=" + isSecure() +
                ", metadata=" + getMetadata() +
                ", namespace=" + getNamespace() +
                ", contextPath=" + getContextPath() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloudMapServiceInstance that = (CloudMapServiceInstance) o;
        return getPort() == that.getPort() &&
                isSecure() == that.isSecure() &&
                Objects.equals(getInstanceId(), that.getInstanceId()) &&
                Objects.equals(getServiceId(), that.getServiceId()) &&
                Objects.equals(getHost(), that.getHost()) &&
                Objects.equals(getMetadata(), that.getMetadata()) &&
                Objects.equals(getContextPath(), that.getContextPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInstanceId(), getServiceId(), getHost(), getPort(), isSecure(), getMetadata(),
                getNamespace(), getContextPath());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, String> metadata = Collections.emptyMap();
        private String serviceId;
        private String namespace;
        private String instanceId;
        private String host;
        private int port = -1;
        private String contextPath = null;
        private boolean secure;

        public Builder withServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder withNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Builder withInstanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withSecure(boolean secure) {
            this.secure = secure;
            return this;
        }

        public Builder withContextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public Builder withMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public CloudMapServiceInstance build() {

            return new CloudMapServiceInstance(instanceId, serviceId, host, port, secure, metadata, namespace, contextPath);
        }
    }
}

package org.springframework.cloud.aws.cloudmap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(CloudMapProperties.CONFIG_PREFIX)
@Validated
public class CloudMapProperties {

    public static final String CONFIG_PREFIX = "aws.cloudmap";

    public static class Service {
        @NotNull
        @NotEmpty
        @Pattern(regexp = "[a-zA-Z][-a-zA-Z0-9_]{3,1022}[a-zA-Z]")
        private String namespace = "application";

        /** Alternative to spring.application.name for registering service */
        @Pattern(regexp = "((?=^.{1,127}$)^([a-zA-Z0-9_][a-zA-Z0-9-_]{0,61}[a-zA-Z0-9_]|[a-zA-Z0-9])(\\.([a-zA-Z0-9_][a-zA-Z0-9-_]{0,61}[a-zA-Z0-9_]|[a-zA-Z0-9]))*$)|(^\\.$)")
        private String name = null;

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Instance {
        private String hostname = null;
        private String ipAddress = null;
        private boolean preferIpAddress = false;
        private boolean securePortEnabled = false;

        public Map<String, String> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }

        private Map<String, String> metadata = new LinkedHashMap<String, String>();

        public boolean isSecurePortEnabled() {
            return securePortEnabled;
        }

        public void setSecurePortEnabled(boolean securePortEnabled) {
            this.securePortEnabled = securePortEnabled;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public boolean isPreferIpAddress() {
            return preferIpAddress;
        }

        public void setPreferIpAddress(boolean preferIpAddress) {
            this.preferIpAddress = preferIpAddress;
        }
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    private Service service;
    private Instance instance;
    private boolean enabled = true;
    private boolean register = true;
    private String region;
    private boolean createNamespace = false;
    private boolean createService = true;

    public boolean isCreateNamespace() {
        return createNamespace;
    }

    public void setCreateNamespace(boolean createNamespace) {
        this.createNamespace = createNamespace;
    }

    public boolean isCreateService() {
        return createService;
    }

    public void setCreateService(boolean createService) {
        this.createService = createService;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isRegister() {
        return register;
    }

    public void setRegister(boolean register) {
        this.register = register;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}

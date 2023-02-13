package com.sl.mecm.authentication.server.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StringUtils;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "mecm.clients")
@PropertySource(value = "classpath:configs/client_certs.yaml", factory = YamlPropertySourceFactory.class)
public class ClientCertsConfig implements InitializingBean {

    private Map<String, ClientCert> certsMap;

    public Map<String, ClientCert> getCertsMap() {
        return certsMap;
    }

    public void setCertsMap(Map<String, ClientCert> certsMap) {
        this.certsMap = certsMap;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.certsMap == null || this.certsMap.isEmpty()){
            throw new RuntimeException("certs is null");
        }
    }

    public static class ClientCert{
        private String credential;
        private String clientId;
        private String clientSecret;

        public String getCredential() {
            return credential;
        }

        public void setCredential(String credential) {
            this.credential = credential;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public boolean checkValues(){
            return StringUtils.hasText(credential) && StringUtils.hasText(clientId) && StringUtils.hasText(clientSecret);
        }

        @Override
        public String toString() {
            return "ClientCert{" +
                    "credential='" + credential + '\'' +
                    ", clientId='" + clientId + '\'' +
                    ", clientSecret='" + clientSecret + '\'' +
                    '}';
        }
    }
}

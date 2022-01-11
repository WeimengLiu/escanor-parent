package com.escanor.gateway.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("gateway.security")
public class GatewaySecurityProperties {
    private String[] permitUrls = new String[]{"/login"};

    public String[] getPermitUrls() {
        return permitUrls;
    }

    public void setPermitUrls(String[] permitUrls) {
        this.permitUrls = permitUrls;
    }
}

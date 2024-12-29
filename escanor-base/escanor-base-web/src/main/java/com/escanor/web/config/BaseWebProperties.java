package com.escanor.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "escanor.web")
public class BaseWebProperties {
    private String[] ignoreWrapResponseUrls = new String[]{};

    private boolean logAspect = false;
}

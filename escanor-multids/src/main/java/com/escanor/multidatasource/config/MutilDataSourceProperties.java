package com.escanor.multidatasource.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("spring")
public class MutilDataSourceProperties {
    private Map<String, MyHikariConfig> dataSources = new HashMap<>();

    public Map<String, MyHikariConfig> getDataSources() {
        return dataSources;
    }

    public void setDataSources(Map<String, MyHikariConfig> dataSources) {
        this.dataSources = dataSources;
    }

}

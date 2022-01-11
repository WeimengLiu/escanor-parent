package com.escanor.multidatasource.repository;

import com.escanor.multidatasource.common.ContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.Assert;

public class MyRouteDataSource extends AbstractRoutingDataSource {

    private String defaultKey;

    @Override
    protected Object determineCurrentLookupKey() {
        if (ContextHolder.isApplicationReady()) {
            Assert.notNull(ContextHolder.getCode(), "code not allow null");
            return ContextHolder.getCode();
        } else {
            return defaultKey;
        }

    }

    public String getDefaultKey() {
        return defaultKey;
    }

    public void setDefaultKey(String defaultKey) {
        this.defaultKey = defaultKey;
    }
}

package com.escanor.jpa.config.multids;

import com.escanor.core.ContextHolder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.Assert;

@Setter
@Getter
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

}

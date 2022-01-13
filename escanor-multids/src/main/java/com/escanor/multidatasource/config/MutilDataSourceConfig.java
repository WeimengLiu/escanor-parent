package com.escanor.multidatasource.config;

import com.escanor.multidatasource.repository.MyRouteDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.MapDataSourceLookup;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(MutilDataSourceProperties.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnExpression("${spring.enableMultiDs:false}")
public class MutilDataSourceConfig {

    @Bean
    MyRouteDataSource dataSource(MutilDataSourceProperties mutilDataSourceProperties) {
        Assert.notNull(mutilDataSourceProperties, "数据源配置不能为空");
        Assert.notEmpty(mutilDataSourceProperties.getDataSources(), "数据源配置不能为空");
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        Map<Object, Object> targetDataSources = new HashMap<>();
        String defaultKey = null;
        for (Map.Entry<String, MyHikariConfig> dataSourcePropertiesEntry : mutilDataSourceProperties.getDataSources().entrySet()) {
            MyHikariConfig myHikariConfig = dataSourcePropertiesEntry.getValue();
            HikariDataSource dataSource = new HikariDataSource();
            myHikariConfig.copyStateTo(dataSource);
            dataSource.setPoolName(dataSourcePropertiesEntry.getKey());
            dataSourceMap.put(dataSourcePropertiesEntry.getKey(), dataSource);
            if (ArrayUtils.isNotEmpty(myHikariConfig.getSupports())) {
                for (String bank : myHikariConfig.getSupports()) {
                    if (StringUtils.isEmpty(defaultKey)) {
                        defaultKey = bank;
                    }
                    targetDataSources.put(bank, dataSourcePropertiesEntry.getKey());
                }
            }
        }
        MyRouteDataSource myRouteDataSource = new MyRouteDataSource();
        myRouteDataSource.setDataSourceLookup(new MapDataSourceLookup(dataSourceMap));
        myRouteDataSource.setTargetDataSources(targetDataSources);
        assert defaultKey != null;
        myRouteDataSource.setDefaultKey(defaultKey);
        return myRouteDataSource;
    }
}

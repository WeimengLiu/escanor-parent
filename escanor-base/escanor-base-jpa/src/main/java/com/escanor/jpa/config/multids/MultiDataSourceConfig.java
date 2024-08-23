/*
 * Copyright (c) 2024 Weimeng Liu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.escanor.jpa.config.multids;

import com.zaxxer.hikari.HikariDataSource;
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
@EnableConfigurationProperties(MultiDataSourceProperties.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnExpression("${spring.enableMultiDs:false}")
public class MultiDataSourceConfig {

    @Bean
    MyRouteDataSource dataSource(MultiDataSourceProperties mutilDataSourceProperties) {
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
            String[] supports = myHikariConfig.getSupports();
            if (null != supports) {
                for (String bank : supports) {
                    if (!StringUtils.hasText(defaultKey)) {
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

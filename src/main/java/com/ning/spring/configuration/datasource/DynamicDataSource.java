package com.ning.spring.configuration.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @Author 二木
 * @Description
 * @Date 2023/11/23 20:36
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContext.get();
    }


}

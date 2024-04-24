package com.ning.spring.configuration.datasource;

import lombok.Getter;

/**
 * @Author 二木
 * @Description
 * @Date 2023/11/23 20:34
 */
@Getter
public enum DataSourceType {
    DEFAULT_DATASOURCE("default", "默认数据库"),
    PLAYER_DATASOURCE("playerdata", "玩家数据库"),
    PLAYER_ECONOMY("economy", "经济数据库");

    private final String dataSourceName;

    private final String desc;

    DataSourceType(String dataSourceName, String desc) {
        this.dataSourceName = dataSourceName;
        this.desc = desc;
    }

    public static DataSourceType findByDataSourceName(String dataSourceName){
        for (DataSourceType value : DataSourceType.values()) {
            if(value.getDataSourceName().equals(dataSourceName)){
                return value;
            }
        }
        throw new IllegalStateException("没有找到 " + dataSourceName +" 对应的数据源");
    }
}

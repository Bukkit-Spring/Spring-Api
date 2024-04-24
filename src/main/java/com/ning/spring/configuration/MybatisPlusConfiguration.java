package com.ning.spring.configuration;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.pagehelper.PageInterceptor;
import com.ning.spring.Main;
import com.ning.spring.configuration.datasource.DataSourceType;
import com.ning.spring.configuration.datasource.DynamicDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Er_mu
 * @description
 * @date 2022/5/4 17:39
 */
@Configuration
@MapperScan(value = {"com.ning.**.mapper*", "aosuo.ning.**.mapper*"})
public class MybatisPlusConfiguration {
    @Resource
    private Environment env;

    @Primary
    @Bean(name = "defaultDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.default")
    public DataSource defaultDataSource() {
        HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder.create(Main.getLoader()).build();
        initHikariConfig(dataSource, "spring.datasource.default");
        return dataSource;
    }

    @Bean(name = "playerDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.playerdata")
    public DataSource playerDataSource() {
        HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder.create(Main.getLoader()).build();
        initHikariConfig(dataSource, "spring.datasource.playerdata");
        return dataSource;
    }

    @Bean(name = "economyDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.economy")
    public DataSource economyDataSource() {
        HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder.create(Main.getLoader()).build();
        initHikariConfig(dataSource, "spring.datasource.economy");
        return dataSource;
    }

    private void initHikariConfig(HikariDataSource dataSource, String prefix){
        dataSource.setConnectionTimeout(env.getProperty(prefix + ".hikari.connection-timeout", Long.class, 30000L));
        dataSource.setMinimumIdle(env.getProperty(prefix + ".hikari.minimum-idle", Integer.class, 20));
        dataSource.setMaximumPoolSize(env.getProperty(prefix + ".hikari.maximum-pool-size", Integer.class, 80));
        dataSource.setAutoCommit(env.getProperty(prefix + ".hikari.auto-commit", Boolean.class, true));
        dataSource.setIdleTimeout(env.getProperty(prefix + ".hikari.idle-timeout", Long.class, 600000L));
        dataSource.setPoolName(env.getProperty(prefix + ".hikari.pool-name", String.class, "HikariCP"));
        dataSource.setRegisterMbeans(env.getProperty(prefix + ".hikari.register-mbeans", Boolean.class, false));
        dataSource.setMaxLifetime(env.getProperty(prefix + ".hikari.max-lifetime", Long.class, 1800000L));
        dataSource.setConnectionTestQuery(env.getProperty(prefix + ".hikari.connection-test-query", String.class, "select now()"));
    }

    @Bean(name = "dynamicDataSource")
    public DynamicDataSource dataSource(@Qualifier("defaultDataSource") DataSource defaultDataSource,
                                        @Qualifier("playerDataSource") DataSource playerDataSource,
                                        @Qualifier("economyDataSource") DataSource economyDataSource) {
        Map<Object, Object> allDatasource = new HashMap<>();

        allDatasource.put(DataSourceType.DEFAULT_DATASOURCE.getDataSourceName(), defaultDataSource);
        allDatasource.put(DataSourceType.PLAYER_DATASOURCE.getDataSourceName(), playerDataSource);
        allDatasource.put(DataSourceType.PLAYER_ECONOMY.getDataSourceName(), economyDataSource);

        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(allDatasource);
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        dynamicDataSource.afterPropertiesSet();
        return dynamicDataSource;
    }


    @Bean("mpConfig")
    public GlobalConfig mpGlobalConfig() {
        GlobalConfig config = new GlobalConfig();
        config.setBanner(false);
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setIdType(IdType.ASSIGN_ID);
        dbConfig.setTableUnderline(true);
        config.setDbConfig(dbConfig);
        return config;
    }

    @Bean
    public DataSourceTransactionManager dataSourceTransactionManager(@Qualifier("dynamicDataSource") DynamicDataSource dynamicDataSource){
        return new DataSourceTransactionManager(dynamicDataSource);
    }

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dynamicDataSource") DynamicDataSource dynamicDataSource, @Qualifier("mpConfig") GlobalConfig config) throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dynamicDataSource);
        sqlSessionFactoryBean.setGlobalConfig(config);
        sqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(Main.getResourceLoader());
        try {
            List<String> mappers = new ArrayList<>();
            mappers.add("classpath*:com/ning/**/xml/*Mapper.xml");
            mappers.add("classpath*:aosuo/ning/**/xml/*Mapper.xml");
            sqlSessionFactoryBean.setMapperLocations(mappers.stream().flatMap(location -> {
                try {
                    return Stream.of(resolver.getResources(location));
                } catch (IOException e) {
                    return Stream.of();
                }
            }).toArray(org.springframework.core.io.Resource[]::new));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sqlSessionFactoryBean.getObject();
    }
}

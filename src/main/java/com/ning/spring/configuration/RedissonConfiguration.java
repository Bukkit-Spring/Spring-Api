package com.ning.spring.configuration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @Author 二木
 * @Description
 * @Date 2023/11/3 17:08
 */
@Configuration
public class RedissonConfiguration {
    @Bean
    public RedissonClient redisClient(RedisProperties properties){
        Config config = new Config();
        //单机redis
        SingleServerConfig singleConfig = config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort());
        if(StringUtils.hasText(properties.getPassword())){
            singleConfig.setPassword(properties.getPassword());
        }
        singleConfig.setTimeout((int) properties.getTimeout().toMillis());
        singleConfig.setPingConnectionInterval(30000);
        singleConfig.setDatabase(properties.getDatabase());
        config.setLockWatchdogTimeout(30000);
        return Redisson.create(config);
    }
}

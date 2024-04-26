package com.ning.spring;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

/**
 * @Author 二木
 * @Description
 * @Date 2023/3/13 16:13
 */
@EnableCaching
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication(scanBasePackages = {"com.ning", "aosuo.ning","top.maplex"})
public class SpringLoader {

    @SneakyThrows
    public static ApplicationContext init(DefaultResourceLoader defaultResourceLoader) {
        //切换配置文件
        Main.getInstance().saveResource("spring.yml", false);
        File file = new File(Main.getInstance().getDataFolder(), "spring.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        Properties properties = new Properties();
        for (String key : config.getKeys(true)) {
            properties.setProperty(key, config.get(key).toString());
        }
        SpringApplication springApplication = new SpringApplication(defaultResourceLoader, SpringLoader.class);
        springApplication.setDefaultProperties(properties);

        //mybatis plus
        Class<MybatisPlusProperties> clazz = MybatisPlusProperties.class;
        Field resourceResolverField = clazz.getDeclaredField("resourceResolver");
        resourceResolverField.setAccessible(true);

        PathMatchingResourcePatternResolver sourceResolve = (PathMatchingResourcePatternResolver) resourceResolverField.get(null);
        Field sourceResourceLoaderField = sourceResolve.getClass().getDeclaredField("resourceLoader");
        sourceResourceLoaderField.setAccessible(true);

        sourceResourceLoaderField.set(sourceResolve, defaultResourceLoader);
        return springApplication.run();
    }


}

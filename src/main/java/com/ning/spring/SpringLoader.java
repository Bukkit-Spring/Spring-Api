package com.ning.spring;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
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
@SpringBootApplication(scanBasePackages = {"com.ning", "aosuo.ning"})
public class SpringLoader {
    @SneakyThrows
    public static ApplicationContext init(DefaultResourceLoader defaultResourceLoader) {
        //切换配置文件
        InputStream config = SpringLoader.class.getClassLoader().getResourceAsStream("default");
        if (config == null) {
            throw new IllegalStateException("插件配置文件异常");
        }
        File applicationProperties = new File(Main.getInstance().getDataFolder(), "application.properties");
        if (!applicationProperties.exists()) {
            applicationProperties.getParentFile().mkdirs();
            //拷贝
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(config, StandardCharsets.UTF_8));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(applicationProperties.toPath()), StandardCharsets.UTF_8))) {
                IOUtils.copy(reader, writer);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        Properties properties = new Properties();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(applicationProperties.toPath()), StandardCharsets.UTF_8))){
            properties.load(br);
        }catch (Exception e){
            throw new IllegalStateException(e);
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

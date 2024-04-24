package com.ning.spring.configuration.datasource;

import org.springframework.lang.Nullable;

/**
 * @Author 二木
 * @Description
 * @Date 2023/11/23 20:36
 */
public class DynamicDataSourceContext {
    private static final ThreadLocal<String> LOCAL_DATASOURCE = new ThreadLocal<>();

    public static void set(String name){
        LOCAL_DATASOURCE.set(name);
    }

    @Nullable
    public static String get(){
        return LOCAL_DATASOURCE.get();
    }

    public static void remove(){
        LOCAL_DATASOURCE.remove();
    }

}

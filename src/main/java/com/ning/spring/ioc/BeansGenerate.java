package com.ning.spring.ioc;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface BeansGenerate {
    Class<? extends Annotation> getAnnotation();

    //获取已经被构建的类实例
    Map<String, Object> getBuildObjects();

    String getBeanName(Class<?> cls);

    void addObject(String name,Object instance);

    void generateBeans();
}

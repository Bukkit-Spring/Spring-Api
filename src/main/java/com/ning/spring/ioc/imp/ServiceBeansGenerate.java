package com.ning.spring.ioc.imp;

import com.ning.spring.api.anntation.Service;
import com.ning.spring.ioc.BeansGenerate;
import com.ning.spring.ioc.Store;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class ServiceBeansGenerate implements BeansGenerate {

    Store store;
    Map<String,Object> builds;

    public ServiceBeansGenerate(Store store) {
        this.store = store;
        this.builds = new HashMap<>();
    }

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return Service.class;
    }

    @Override
    public Map<String, Object> getBuildObjects() {
        return builds;
    }

    @Override
    public String getBeanName(Class<?> cls) {
        return cls.getAnnotation(Service.class).value().isEmpty() ? cls.getName() : cls.getAnnotation(Service.class).value();
    }

    @Override
    public void addObject(String name, Object instance) {
        builds.put(name,instance);
    }

    @Override
    public void generateBeans() {

    }

    @Override
    public String toString() {
        return builds.toString();
    }
}

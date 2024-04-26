package com.ning.spring.ioc;

import com.ning.spring.api.anntation.Inject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FactoryManager {
    Store store;
    private final Map<Class<?>,BeansGenerate> beansGenerates = new HashMap<>();

    public FactoryManager(Store store) {
        this.store = store;
    }

    public void addPool(Class<?> clazz) throws InstantiationException, IllegalAccessException { // 装载
        for(BeansGenerate generate : beansGenerates.values()){
            if(clazz.isAnnotationPresent(generate.getAnnotation())){
                generate.addObject(generate.getBeanName(clazz),clazz.newInstance());
            }
        }
    }

    public void generatesBeans(){
        for(BeansGenerate generate : beansGenerates.values()){
            generate.generateBeans();
        }
    }

    public void addBeanGenerates(BeansGenerate beansGenerate){
        beansGenerates.put(beansGenerate.getClass(),beansGenerate);
    }

    public List<BeansGenerate> getBeansGenerates(){
        return new ArrayList<>(beansGenerates.values());
    }

    public BeansGenerate getBeansGenerate(Class<? extends BeansGenerate> g){
        return beansGenerates.get(g);
    }

    public void injectObject(Map<String,Object> objectMap, Inject inject, Object object, Field field) throws IllegalAccessException {
        Object value;
        value = !inject.value().isEmpty() ? objectMap.get(inject.value()) : objectMap.get(field.getType().getName());
        field.setAccessible(true);
        field.set(object,value);
    }
}

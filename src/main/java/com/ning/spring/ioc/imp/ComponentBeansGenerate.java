package com.ning.spring.ioc.imp;

import com.ning.spring.api.anntation.Bean;
import com.ning.spring.api.anntation.Component;
import com.ning.spring.api.anntation.Inject;
import com.ning.spring.ioc.BeansGenerate;
import com.ning.spring.ioc.Store;
import com.ning.spring.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ComponentBeansGenerate implements BeansGenerate {
    Store store;
    Map<String,Object> builds;

    public ComponentBeansGenerate(Store store) {
        this.store = store;
        this.builds = new HashMap<>();
    }

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return Component.class;
    }

    @Override
    public Map<String, Object> getBuildObjects() {
        return builds;
    }

    @Override
    public String getBeanName(Class<?> cls) {
        return cls.getAnnotation(Component.class).value().isEmpty() ? cls.getName() : cls.getAnnotation(Component.class).value();
    }

    @Override
    public void addObject(String name, Object instance) {
        builds.put(name,instance);
    }

    @Override
    public void generateBeans() {
        for(Object object : getBuildObjects().values()){
            List<Field> fields = ClassUtil.getAllField(object.getClass(), Inject.class);
            BeansGenerate generate = store.getManager().getBeansGenerate(ServiceBeansGenerate.class);

            for(Field field : fields)
                try {
                    store.getManager().injectObject(generate.getBuildObjects(), field.getAnnotation(Inject.class), object, field);
                }catch (IllegalAccessException e){
                    store.getInstance().getLogger().log(Level.WARNING,"Cannot inject bean.",e);
                }

            for(Method method : object.getClass().getMethods()){ //方法注入
                if(method.isAnnotationPresent(Bean.class)){
                    try {
                        addObject(method.getAnnotation(Bean.class).value(),method.invoke(object));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        store.getInstance().getLogger().log(Level.WARNING,"Cannot invoke method.",e);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return builds.toString();
    }
}

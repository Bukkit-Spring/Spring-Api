package com.ning.spring.ioc;

import com.ning.spring.ioc.imp.ComponentBeansGenerate;
import com.ning.spring.ioc.imp.ServiceBeansGenerate;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class Store {
    JavaPlugin instance;
    FactoryManager manager;

    public Store(JavaPlugin instance){
        this.instance = instance;
        this.manager = new FactoryManager(this);
        this.manager.addBeanGenerates(new ServiceBeansGenerate(this));
        this.manager.addBeanGenerates(new ComponentBeansGenerate(this));
    }

    public void build(){
        try {
            List<BeansGenerate> beansGenerates = this.manager.getBeansGenerates();
            ClassScanner classScanner = new ClassScanner("", true, null,
                    cls -> beansGenerates
                            .stream()
                            .anyMatch(item -> cls.isAnnotationPresent(item.getAnnotation())),
                    instance,
                    instance.getClass().getClassLoader());
            Set<Class<?>> classes = classScanner.doScanAllClasses();
            for(Class<?> clazz : classes)
                manager.addPool(clazz);
            manager.generatesBeans();
        }catch (Exception e){
            this.instance.getLogger().log(Level.WARNING,"Cannot build beans.",e);
        }
    }

    public JavaPlugin getInstance() {
        return instance;
    }

    public FactoryManager getManager() {
        return manager;
    }
}

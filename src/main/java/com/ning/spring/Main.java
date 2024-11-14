package com.ning.spring;

import com.ning.spring.classloader.RySpringClassLoader;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @Author 二木
 * @Description
 * @Date 2023/3/13 16:01
 */
@Getter
public class Main extends JavaPlugin {
    private static Main instance;

    private static RySpringClassLoader loader;

    private static DefaultResourceLoader resourceLoader;

    private ApplicationContext applicationContext;


    @Override
    public void onEnable() {
        instance = this;
        Enhancer enhancer = getEnhancer();
        loader = (RySpringClassLoader) enhancer.create(new Class[]{List.class}, new Object[]{getAllPluginLoader()});
        resourceLoader = new DefaultResourceLoader(loader);
        applicationContext = SpringLoader.init(resourceLoader);
    }

    public static Main getInstance() {
        return instance;
    }

    public static RySpringClassLoader getLoader() {
        return loader;
    }

    public static DefaultResourceLoader getResourceLoader() {
        return resourceLoader;
    }


    @SneakyThrows
    private static List<ClassLoader> getAllPluginLoader() {
        List<String> arrays = Arrays.asList(SpringLoader.class.getAnnotation(SpringBootApplication.class).scanBasePackages());
        return Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .filter(item -> isInPackage(arrays, item.getDescription().getMain()))
                .map(item -> item.getClass().getClassLoader())
                .collect(Collectors.toList());
    }

    private static Boolean isInPackage(List<String> springScanPackages, String mainClass) {
        for (String springScanPackage : springScanPackages) {
            if (mainClass.startsWith(springScanPackage)) {
                return true;
            }
        }
        return false;
    }

    private static Enhancer getEnhancer() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(RySpringClassLoader.class);
        enhancer.setCallback((MethodInterceptor) (object, method, args, methodProxy) -> {
            String name = method.getName();
            if ("getResourceAsStream".equals(name) || "getResource".equals(name) || "getResources".equals(name) || "loadClass".equals(name)) {
                //这四个调用新的方法
                return methodProxy.invokeSuper(object, args);
            }
            //其余调用原有classLoader的
            Field currentLoaderField = getDeclaredMethod(object, "currentLoader");
            if (currentLoaderField == null) {
                Main.getInstance().getLogger().warning("currentLoader为空");
                return null;
            }
            currentLoaderField.setAccessible(true);
            Object currentLoader = currentLoaderField.get(object);
            if (currentLoader == null) {
                Main.getInstance().getLogger().warning("loader为空");
                return null;
            }
            Method originMethod = getDeclaredMethod(currentLoader, method.getName(), method.getParameterTypes());
            if (originMethod == null) {
                Main.getInstance().getLogger().warning("方法为空");
                return null;
            }
            originMethod.setAccessible(true);
            return originMethod.invoke(currentLoader, args);
        });
        return enhancer;
    }


    /**
     * 循环向上转型, 获取对象的 DeclaredMethod
     *
     * @param object         : 子类对象
     * @param methodName     : 父类中的方法名
     * @param parameterTypes : 父类中的方法参数类型
     * @return 父类中的方法对象
     */

    private static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) {
        try {
            //先尝试获取普通方法
            return object.getClass().getMethod(methodName, parameterTypes);
        } catch (Exception e) {
        }
        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredMethod(methodName, parameterTypes);
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了

            }
        }
        return null;
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredField
     *
     * @param object    : 子类对象
     * @param fieldName : 父类中的属性名
     * @return 父类中的方法对象
     */

    private static Field getDeclaredMethod(Object object, String fieldName) {
        for (Class<?> clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (Exception e) {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }
        return null;
    }
}

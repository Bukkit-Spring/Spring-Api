package com.ning.spring.classloader;

import com.ning.spring.SpringLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * @author Er_mu
 * @description
 * @date 2022/5/9 18:37
 */
public class RySpringClassLoader extends URLClassLoader {
    /**
     * 当前插件的类加载器
     */
    private final ClassLoader currentLoader;

    /**
     * 所有插件的类加载器
     */
    private final List<ClassLoader> classLoaderList;

    public RySpringClassLoader(List<ClassLoader> classLoaderList) {
        super(new URL[]{}, SpringLoader.class.getClassLoader());
        this.classLoaderList = classLoaderList;
        this.currentLoader = SpringLoader.class.getClassLoader();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        //先查当前插件
        InputStream resourceAsStream = currentLoader.getResourceAsStream(name);
        if (resourceAsStream != null) {
            return resourceAsStream;
        }
        //搜索所有插件的
        for (ClassLoader classLoader : classLoaderList) {
            resourceAsStream = classLoader.getResourceAsStream(name);
            if (resourceAsStream != null) {
                return resourceAsStream;
            }
        }
        return null;
    }

    @Override
    public URL getResource(String name) {
        //先查当前插件
        URL url = currentLoader.getResource(name);
        if (url != null) {
            return url;
        }
        //搜索所有插件的
        for (ClassLoader classLoader : classLoaderList) {
            url = classLoader.getResource(name);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<Enumeration<URL>> list = new ArrayList<>();
        boolean isScanPackage = "com/ning/".equalsIgnoreCase(name) || "aosuo/ning/".equalsIgnoreCase(name);

        //先查当前插件
        Enumeration<URL> enumeration = currentLoader.getResources(name);
        if (enumeration != null) {
            if(!isScanPackage){
                //返回第一个
                return enumeration;
            }
            list.add(enumeration);
        }

        //搜索所有插件的
        for (ClassLoader classLoader : classLoaderList) {
            enumeration = classLoader.getResources(name);
            if (enumeration != null) {
                if(!isScanPackage){
                    //返回第一个
                    return enumeration;
                }
                list.add(enumeration);
            }
        }
        if(!isScanPackage || list.isEmpty()){
            return null;
        }
        Vector<URL> urlList = new Vector<>();
        list.forEach(item -> {
            while (item.hasMoreElements()) {
                urlList.add(item.nextElement());
            }
        });

        return urlList.elements();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return currentLoader.loadClass(name);
    }
}

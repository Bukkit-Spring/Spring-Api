package com.ning.spring.ioc;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {
    private final String basePackage;
    private final boolean recursive;
    private final Predicate<String> packagePredicate;
    private final Predicate<Class> classPredicate;
    private final ClassLoader classLoader;
    private URL jarUrl;


    public ClassScanner(String basePackage, boolean recursive, Predicate<String> packagePredicate,
                        Predicate<Class> classPredicate , Object obj , ClassLoader classLoader) {
        this(basePackage,recursive,packagePredicate,classPredicate,obj.getClass().getProtectionDomain().getCodeSource().getLocation(),classLoader);
    }

    /**
     * Instantiates a new Class scanner.
     *
     * @param basePackage      the base package
     * @param recursive        是否递归扫描
     * @param packagePredicate the package predicate
     * @param classPredicate   the class predicate
     */
    public ClassScanner(String basePackage, boolean recursive, Predicate<String> packagePredicate,
                        Predicate<Class> classPredicate, URL jarUrl, ClassLoader classLoader) {
        this.basePackage = basePackage;
        this.recursive = recursive;
        this.packagePredicate = packagePredicate;
        this.classPredicate = classPredicate;
        this.jarUrl = jarUrl;
        this.classLoader = classLoader;
    }

    /**
     * Do scan all classes set.
     *
     * @return the set
     * @throws IOException            the io exception
     * @throws ClassNotFoundException the class not found exception
     */
    public Set<Class<?>> doScanAllClasses() throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        String packageName = basePackage;
        if (packageName.endsWith(".")) {
            packageName = packageName.substring(0, packageName.lastIndexOf('.'));
        }

        String basePackageFilePath = packageName.replace('.', '/');
        doScanPackageClassesByJar(packageName, jarUrl, classes);
        return classes;
    }

    private void doScanPackageClassesByJar(String basePackage, URL url, Set<Class<?>> classes)
            throws IOException, ClassNotFoundException {
        String packageName = basePackage;
        String basePackageFilePath = packageName.replace('.', '/');
        JarFile jar = new JarFile(URLDecoder.decode(url.getPath(),"UTF-8"));
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (!name.startsWith(basePackageFilePath) || entry.isDirectory()) {
                continue;
            }
            if (!recursive && name.lastIndexOf('/') != basePackageFilePath.length()) {
                continue;
            }

            if (packagePredicate != null) {
                String jarPackageName = name.substring(0, name.lastIndexOf('/')).replace("/", ".");
                if (!packagePredicate.test(jarPackageName)) {
                    continue;
                }
            }

            String className = name.replace('/', '.');
            if (!className.endsWith(".class")) {
                continue;
            }
            className = className.substring(0, className.length() - 6);
            Class<?> loadClass =  classLoader.loadClass(className);
            if (classPredicate == null || classPredicate.test(loadClass)) {
                classes.add(loadClass);
            }

        }
    }

    private void doScanPackageClassesByFile(Set<Class<?>> classes, String packageName, String packagePath)
            throws ClassNotFoundException {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirFiles = dir.listFiles((FileFilter) file -> {
            String filename = file.getName();

            if (file.isDirectory()) {
                if (!recursive) {
                    return false;
                }

                if (packagePredicate != null) {
                    return packagePredicate.test(packageName + "." + filename);
                }
                return true;
            }

            return filename.endsWith(".class");
        });

        if (null == dirFiles) {
            return;
        }

        for (File file : dirFiles) {
            if (file.isDirectory()) {
                doScanPackageClassesByFile(classes, packageName + "." + file.getName(), file.getAbsolutePath());
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                Class<?> loadClass = Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className);
                if (classPredicate == null || classPredicate.test(loadClass)) {
                    classes.add(loadClass);
                }
            }
        }
    }
}

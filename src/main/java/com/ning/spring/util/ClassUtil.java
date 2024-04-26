package com.ning.spring.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ClassUtil {

    public static List<Field> getAllField(Class<?> clazz,Class<? extends Annotation> annotation){
        List<Field> fields = new ArrayList<>();
        for(Field field : clazz.getDeclaredFields()){
            if(field.isAnnotationPresent(annotation)){
                fields.add(field);
            }
        }
        return fields;
    }
}

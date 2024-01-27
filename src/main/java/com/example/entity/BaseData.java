package com.example.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Consumer;

public interface BaseData {

    default <V> V asViewObject(Class<V> clazz, Consumer<V> consumer){
        V v = this.asViewObject(clazz);
        consumer.accept(v);
        return v;
    }

    default <V> V asViewObject(Class<V> clazz){
        try{
            Field[] declaredFields = clazz.getDeclaredFields();
            Constructor<V> constructor = clazz.getConstructor();
            V v = constructor.newInstance();
            //复制所有的字段
            for (Field declaredField : declaredFields) {
                this.convert(declaredField,v);
            }
            return v;
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    private void convert(Field field,Object vo){
        try{
            //获取到需要复制的数据
            Field source = this.getClass().getDeclaredField(field.getName());
            //设置数据可见
            field.setAccessible(true);
            source.setAccessible(true);
            field.set(vo,source.get(this));

        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }

}

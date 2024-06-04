package com.cool.request.components.method.parameter;

import com.cool.request.components.method.ParameterConvertManager;
import com.cool.request.components.method.ParameterConverter;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ObjectParameterConverter implements ParameterConverter {
    private ParameterConvertManager manager;

    public ObjectParameterConverter(ParameterConvertManager parameterConvertManager) {
        this.manager = parameterConvertManager;
    }

    @Override
    public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
        return value instanceof HashMap;
    }

    @Override
    public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) {
        try {
            Object instance = newInstance(parameterClass);
            BeanUtilsBean beanUtilsBean = new BeanUtilsBean(manager.getPrimitiveParameterConverter().getConvertUtilsBean(), new PropertyUtilsBean());
            BeanUtilsBean.setInstance(beanUtilsBean);
            BeanUtils.populate(instance, ((Map<String, Object>) data));
            if (parameterClass.isArray()) {
                return Array.newInstance(parameterClass.getComponentType(), 1);
            }
            if (List.class.isAssignableFrom(parameterClass)) {
                List<Object> result = new ArrayList<>();
                result.add(instance);
                return result;
            }
            if (Set.class.isAssignableFrom(parameterClass)) {
                Set<Object> result = new HashSet<>();
                result.add(instance);
                return result;
            }
            if (Map.class.isAssignableFrom(parameterClass)) {
                return data;
            }
            return instance;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T newInstance(Class<T> clazz) {
        try {
            if (clazz.isArray()) {
                Class<?> componentTypeClass = clazz.getComponentType();
                Constructor<?> constructor = componentTypeClass.getConstructor();
                return (T) constructor.newInstance();
            }
            Constructor<?> constructor = clazz.getConstructor();
            return (T) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

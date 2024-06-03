package com.cool.request.components.method.parameter;

import com.cool.request.components.method.ParameterConvertManager;
import com.cool.request.components.method.ParameterConverter;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ObjectParameterConverter implements ParameterConverter {
    private ParameterConvertManager manager;

    public ObjectParameterConverter(ParameterConvertManager parameterConvertManager) {
        this.manager = parameterConvertManager;
    }

    @Override
    public boolean canSupport(Class<?> parameterClass, Object value) {
        return value instanceof HashMap;
    }

    @Override
    public Object converter(Class<?> parameterClass, Object data) {
        try {
            Constructor<?> constructor = parameterClass.getConstructor();
            Object instance = constructor.newInstance();
            BeanUtils.populate(instance, ((Map<String, Object>) data));
            return instance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}

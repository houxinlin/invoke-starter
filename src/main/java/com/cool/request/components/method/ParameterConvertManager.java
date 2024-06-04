package com.cool.request.components.method;

import com.cool.request.components.method.parameter.ObjectParameterConverter;
import com.cool.request.components.method.parameter.ParameterConverters;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ParameterConvertManager {
    private List<ParameterConverter> parameterConverters = new ArrayList<>();
    private ParameterConverters.PrimitiveParameterConverter primitiveParameterConverter = new ParameterConverters.PrimitiveParameterConverter();

    public ParameterConvertManager() {
        parameterConverters.add(new ParameterConverters.ByteArrayParameterConverter());
        parameterConverters.add(new ParameterConverters.HttpServletRequestParameterConverter());
        parameterConverters.add(new ParameterConverters.HttpServletResponseParameterConverter());
        parameterConverters.add(new ParameterConverters.PrimitiveParameterConverter());
        parameterConverters.add(new ObjectParameterConverter(this));
    }

    public ParameterConverters.PrimitiveParameterConverter getPrimitiveParameterConverter() {
        return primitiveParameterConverter;
    }

    public Object getConvertValue(Method method, int parameterIndex, Class<?> clazz, Object data) throws ParseException {
        for (ParameterConverter parameterConverter : parameterConverters) {
            if (parameterConverter.canSupport(method, parameterIndex, clazz, data)) {
                return parameterConverter.converter(method, parameterIndex, clazz, data);
            }
        }
        return null;
    }

}

package com.cool.request.components.method;

import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.components.method.parameter.ObjectParameterConverter;
import com.cool.request.components.method.parameter.ParameterConverters;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ParameterConvertManager {
    private static final Logger log = Logger.getLogger(ParameterConvertManager.class.getName());
    private final List<ParameterConverter> parameterConverters = new ArrayList<>();
    private final ParameterConverters.PrimitiveParameterConverter primitiveParameterConverter = new ParameterConverters.PrimitiveParameterConverter();

    public ParameterConvertManager(SpringBootStartInfo springBootStartInfo) {
        parameterConverters.add(new ParameterConverters.MapParameterConverter(springBootStartInfo));
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
        log.info("Can't find parameter converter for method " + method.getName() + ", parameter index " + parameterIndex);
        return null;
    }

}

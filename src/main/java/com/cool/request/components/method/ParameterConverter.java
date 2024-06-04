package com.cool.request.components.method;

import java.lang.reflect.Method;
import java.text.ParseException;

public interface ParameterConverter {
    public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value);

    public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) throws ParseException;
}

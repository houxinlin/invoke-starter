package com.cool.request.components.method;

import java.text.ParseException;

public interface ParameterConverter {
    public boolean canSupport(Class<?> parameterClass,Object value);

    public Object converter(Class<?> parameterClass,Object data) throws ParseException;
}

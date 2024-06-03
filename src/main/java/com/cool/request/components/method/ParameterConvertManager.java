package com.cool.request.components.method;

import com.cool.request.components.method.parameter.ObjectParameterConverter;
import com.cool.request.components.method.parameter.ParameterConverters;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ParameterConvertManager {
    private List<ParameterConverter> parameterConverters = new ArrayList<>();

    public ParameterConvertManager() {
        parameterConverters.add(new ParameterConverters.ByteArrayParameterConverter());
        parameterConverters.add(new ParameterConverters.DoubleParameterConverter());
        parameterConverters.add(new ParameterConverters.FloatParameterConverter());
        parameterConverters.add(new ParameterConverters.IntParameterConverter());
        parameterConverters.add(new ParameterConverters.StringParameterConverter());
        parameterConverters.add(new ParameterConverters.LongParameterConverter());
        parameterConverters.add(new ParameterConverters.DateTimeParameterConverter());
        parameterConverters.add(new ParameterConverters.BooleanParameterConverter());
        parameterConverters.add(new ObjectParameterConverter(this));

    }

    public Object getConvertValue(Class<?> clazz, Object data) throws ParseException {
        for (ParameterConverter parameterConverter : parameterConverters) {
            if (parameterConverter.canSupport(clazz, data)) {
                return parameterConverter.converter(clazz, data);
            }
        }
        return null;
    }

    public boolean canSupport(Class<?> clazz, Object value) {
        for (ParameterConverter parameterConverter : parameterConverters) {
            if (parameterConverter.canSupport(clazz, value)) return true;
        }
        return false;
    }
}

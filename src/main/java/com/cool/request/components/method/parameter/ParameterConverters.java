package com.cool.request.components.method.parameter;

import com.cool.request.components.method.ParameterConverter;
import com.cool.request.utils.DateTimeUtils;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class ParameterConverters {
    public static class ByteArrayParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Class<?> parameterClass, Object value) {
            return byte[].class.equals(parameterClass);
        }

        @Override
        public Object converter(Class<?> parameterClass, Object data) throws ParseException {
            return data == null ? null : data.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    public static class StringParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Class<?> parameterClass, Object value) {
            return String.class.equals(parameterClass);
        }

        @Override
        public Object converter(Class<?> parameterClass, Object data) {
            return data == null ? null : data.toString();
        }
    }

    public static class FloatParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Class<?> parameterClass, Object value) {
            return float.class.equals(parameterClass) || Float.class.equals(parameterClass);
        }

        @Override
        public Object converter(Class<?> parameterClass, Object data) {
            return Float.valueOf(data.toString());
        }
    }

    public static class DoubleParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Class<?> parameterClass, Object value) {
            return double.class.equals(parameterClass) || Double.class.equals(parameterClass);
        }

        @Override
        public Object converter(Class<?> parameterClass, Object data) {
            return Double.valueOf(data.toString());
        }
    }


    public static class IntParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Class<?> parameterClass, Object value) {
            return int.class.equals(parameterClass) || Integer.class.equals(parameterClass);
        }

        @Override
        public Object converter(Class<?> parameterClass, Object data) {
            return Integer.valueOf(data.toString());
        }
    }

    public static class LongParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Class<?> parameterClass, Object value) {
            return long.class.equals(parameterClass) || Long.class.equals(parameterClass);
        }

        @Override
        public Object converter(Class<?> parameterClass, Object data) {
            return Long.valueOf(data.toString());
        }
    }

    public static class BooleanParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Class<?> parameterClass, Object value) {
            return boolean.class.equals(parameterClass) || Boolean.class.equals(parameterClass);
        }

        @Override
        public Object converter(Class<?> parameterClass, Object data) {
            return "true".equals(data);
        }
    }

    public static class DateTimeParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Class<?> parameterClass, Object value) {
            return LocalDateTime.class.equals(parameterClass);
        }

        @Override
        public Object converter(Class<?> parameterClass, Object data) throws ParseException {
            List<String> list = Arrays.asList(
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm",
                    "yyyy-MM-dd HH:mm",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy/MM/dd HH:mm:ss",
                    "yyyy/MM/dd HH:mm",
                    "yyyy MM dd HH:mm",
                    "yyyy MM dd HH:mm:ss");
            for (String format : list) {
                try {
                    return DateTimeUtils.toLocalDateTime(data.toString(), format);
                } catch (Exception ignored) {
                }
            }
            throw new ParseException(data.toString(), 0);
        }
    }

}

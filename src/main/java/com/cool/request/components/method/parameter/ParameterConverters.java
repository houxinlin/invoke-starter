package com.cool.request.components.method.parameter;

import com.cool.request.compatible.CompatibilityUtil;
import com.cool.request.components.method.ParameterConverter;
import com.cool.request.utils.DateTimeUtils;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ParameterConverters {
    public static class ByteArrayParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
            return byte[].class.equals(parameterClass);
        }

        @Override
        public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) throws ParseException {
            return data == null ? null : data.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    public static class HttpServletRequestParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
            return CompatibilityUtil.loadHttpServletRequestClass().isAssignableFrom(parameterClass);
        }

        @Override
        public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) throws ParseException {
            return new MockHttpServletRequest();
        }
    }

    public static class HttpServletResponseParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
            return CompatibilityUtil.loadHttpServletResponse().isAssignableFrom(parameterClass);
        }

        @Override
        public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) throws ParseException {
            return new MockHttpServletResponse();
        }
    }

    public static class LocalDateTimeParameterConverter implements Converter {
        @Override
        public <T> T convert(Class<T> aClass, Object data) {
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
                    return aClass.cast(DateTimeUtils.toLocalDateTime(data.toString(), format));
                } catch (Exception ignored) {
                }
            }
            throw new RuntimeException("Can't convert " + data + " to " + aClass);
        }
    }

    public static class LocalDateParameterConverter implements Converter {
        @Override
        public <T> T convert(Class<T> aClass, Object data) {
            List<String> list = Arrays.asList(
                    "yyyy-MM-dd",
                    "yyyy/MM/dd",
                    "yyyy MM dd");
            for (String format : list) {
                try {
                    return aClass.cast(DateTimeUtils.toLocalDateTime(data.toString(), format));
                } catch (Exception ignored) {
                }
            }
            throw new RuntimeException("Can't convert " + data + " to " + aClass);
        }
    }

    public static class URIParameterConverter implements Converter {
        @Override
        public <T> T convert(Class<T> aClass, Object data) {
            return aClass.cast(URI.create(String.valueOf(data)));
        }
    }


    public static class PrimitiveParameterConverter implements ParameterConverter {
        private final ConvertUtilsBean convertUtilsBean = new ConvertUtilsBean();

        public ConvertUtilsBean getConvertUtilsBean() {
            return convertUtilsBean;
        }

        public PrimitiveParameterConverter() {
            convertUtilsBean.register(new LocalDateTimeParameterConverter(), LocalDateTime.class);
            convertUtilsBean.register(new LocalDateParameterConverter(), LocalDate.class);
            convertUtilsBean.register(new URIParameterConverter(), URI.class);
            convertUtilsBean.register(new Converter() {
                @Override
                public <T> T convert(Class<T> aClass, Object o) {
                    if (o == null) return null;
                    List<String> list = Arrays.asList(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            "yyyy-MM-dd'T'HH:mm",
                            "yyyy-MM-dd HH:mm",
                            "yyyy-MM-dd HH:mm:ss",
                            "yyyy/MM/dd HH:mm:ss",
                            "yyyy/MM/dd HH:mm",
                            "yyyy MM dd HH:mm",
                            "yyyy MM dd HH:mm:ss",
                            "yyyy-MM-dd",
                            "yyyy/MM/dd",
                            "yyyy MM dd");
                    for (String format : list) {
                        try {
                            return (T) new SimpleDateFormat(format).parse(o.toString());
                        } catch (ParseException ignored) {
                        }
                    }
                    return null;
                }
            }, Date.class);
        }

        @Override
        public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
            Type genericParameterType = method.getGenericParameterTypes()[parameterIndex];
            if (List.class.isAssignableFrom(parameterClass)) {
                if (genericParameterType instanceof ParameterizedType) {
                    Type actualTypeArgument = ((ParameterizedType) genericParameterType).getActualTypeArguments()[0];
                    return convertUtilsBean.lookup(((Class) actualTypeArgument)) != null;
                }
                return true;
            }
            if (Set.class.isAssignableFrom(parameterClass)) {
                if (genericParameterType instanceof ParameterizedType) {
                    Type actualTypeArgument = ((ParameterizedType) genericParameterType).getActualTypeArguments()[0];
                    return convertUtilsBean.lookup(((Class) actualTypeArgument)) != null;
                }
                return true;
            }
            if (value == null) return convertUtilsBean.lookup(parameterClass) != null;
            return convertUtilsBean.lookup(value.getClass(), parameterClass) != null;
        }

        @Override
        public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) throws ParseException {
            if (List.class.isAssignableFrom(parameterClass)) {
                List<Object> result = new ArrayList<>();
                Class<?> componentType = getGenericParameterTypes(method.getGenericParameterTypes()[parameterIndex]);
                Object instance = Array.newInstance(componentType, 0);
                Object convert = convertUtilsBean.convert(data, instance.getClass());
                if (convert != null) {
                    if (convert.getClass().isArray()) {
                        Collections.addAll(result, (Object[]) convert);
                    }
                }
                return result;
            }
            if (Set.class.isAssignableFrom(parameterClass)) {
                Set<Object> result = new HashSet<>();
                Class<?> componentType = getGenericParameterTypes(method.getGenericParameterTypes()[parameterIndex]);
                Object instance = Array.newInstance(componentType, 0);
                Object convert = convertUtilsBean.convert(data, instance.getClass());
                if (convert != null) {
                    if (convert.getClass().isArray()) {
                        Collections.addAll(result, (Object[]) convert);
                    }
                }
                return result;
            }
            return convertUtilsBean.convert(data, parameterClass);
        }
    }


    private static Class<?> getGenericParameterTypes(Type genericParameterType) {
        if (genericParameterType instanceof ParameterizedType) {
            Type actualTypeArgument = ((ParameterizedType) genericParameterType).getActualTypeArguments()[0];
            return ((Class) actualTypeArgument);
        }
        return Object.class;
    }


}

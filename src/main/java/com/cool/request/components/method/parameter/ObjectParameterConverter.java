package com.cool.request.components.method.parameter;

import com.cool.request.components.method.ParameterConvertManager;
import com.cool.request.components.method.ParameterConverter;
import com.cool.request.json.GsonMapper;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.*;
import java.text.ParseException;
import java.util.*;

public class ObjectParameterConverter implements ParameterConverter {
    private final List<ParameterConverter> parameterConverters = new ArrayList<>();

    public ObjectParameterConverter(ParameterConvertManager parameterConvertManager) {
        parameterConverters.add(new ListParameterConverter());
        parameterConverters.add(new SetParameterConverter());
        parameterConverters.add(new MapParameterConverter());
        parameterConverters.add(new ArrayTypeConverter());
        parameterConverters.add(new ObjectTypeConverter());
    }

    @Override
    public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
        return value instanceof String;
    }

    @Override
    public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) {
        for (ParameterConverter parameterConverter : parameterConverters) {
            if (parameterConverter.canSupport(method, parameterIndex, parameterClass, data)) {
                try {
                    return parameterConverter.converter(method, parameterIndex, parameterClass, data);
                } catch (ParseException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private static class ArrayTypeConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
            return parameterClass.isArray();
        }

        @Override
        public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) {
            Class<?> componentTypeClass = parameterClass.getComponentType();
            return GsonMapper.getGson().fromJson(data.toString(), Array.newInstance(componentTypeClass, 0).getClass());
        }
    }

    private static class ObjectTypeConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
            return !parameterClass.isPrimitive() && !parameterClass.isArray();
        }

        @Override
        public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) {
            if (data == null) return null;
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            if (parameterIndex < genericParameterTypes.length) {
                Type genericParameterType = genericParameterTypes[parameterIndex];
                if (genericParameterType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    Type type = TypeToken.getParameterized(parameterClass, actualTypeArguments).getType();
                    return GsonMapper.getGson().fromJson(data.toString(), type);
                }
            }
            return GsonMapper.getGson().fromJson(data.toString(), parameterClass);
        }
    }

    private static class MapParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
            return Map.class.isAssignableFrom(parameterClass);
        }

        @Override
        public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) {
            if (data == null) return new HashMap<>();
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            return GsonMapper.getGson().fromJson(String.valueOf(data), type);
        }
    }

    private static class SetParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
            return Set.class.isAssignableFrom(parameterClass);
        }

        @Override
        public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) throws ParseException {
            if (data == null) return new HashSet<>();
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            if (parameterIndex < genericParameterTypes.length) {
                Type genericParameterType = genericParameterTypes[parameterIndex];
                if (genericParameterType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length > 0) {
                        String genericParameter = actualTypeArguments[0].getTypeName();
                        try {
                            Class<?> aClass = Class.forName(genericParameter);
                            Type listType = TypeToken.getParameterized(Set.class, aClass).getType();
                            return GsonMapper.getGson().fromJson(data.toString(), listType);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            }
            return new HashSet<>();
        }
    }

    private static class ListParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
            return List.class.isAssignableFrom(parameterClass);
        }

        @Override
        public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) throws ParseException {
            if (data == null) return new ArrayList<>();
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            if (parameterIndex < genericParameterTypes.length) {
                Type genericParameterType = genericParameterTypes[parameterIndex];
                if (genericParameterType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length > 0) {
                        String genericParameter = actualTypeArguments[0].getTypeName();
                        try {
                            Class<?> aClass = Class.forName(genericParameter);
                            Type listType = TypeToken.getParameterized(List.class, aClass).getType();
                            return GsonMapper.getGson().fromJson(data.toString(), listType);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            return new ArrayList<>();
        }
    }
}

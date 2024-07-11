package com.cool.request.components.method.parameter;

import com.cool.request.components.method.ParameterConvertManager;
import com.cool.request.components.method.ParameterConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.*;
import java.text.ParseException;
import java.util.*;

public class ObjectParameterConverter implements ParameterConverter {
    private ParameterConvertManager manager;

    private List<ParameterConverter> parameterConverters = new ArrayList<>();

    public ObjectParameterConverter(ParameterConvertManager parameterConvertManager) {
        this.manager = parameterConvertManager;
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
//        try {
//            Object instance = newInstance(parameterClass);
//            BeanUtilsBean beanUtilsBean = new BeanUtilsBean(manager.getPrimitiveParameterConverter().getConvertUtilsBean(), new PropertyUtilsBean());
//            BeanUtilsBean.setInstance(beanUtilsBean);
//            BeanUtils.populate(instance, ((Map<String, Object>) data));
//            if (parameterClass.isArray()) {
//                return Array.newInstance(parameterClass.getComponentType(), 1);
//            }
//            if (List.class.isAssignableFrom(parameterClass)) {
//                List<Object> result = new ArrayList<>();
//                result.add(instance);
//                return result;
//            }
//            if (Set.class.isAssignableFrom(parameterClass)) {
//                Set<Object> result = new HashSet<>();
//                result.add(instance);
//                return result;
//            }
//            if (Map.class.isAssignableFrom(parameterClass)) {
//                return data;
//            }
//            return instance;
//        } catch (IllegalAccessException | InvocationTargetException e) {
//            throw new RuntimeException(e);
//        }
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

    private static class ArrayTypeConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
            return parameterClass.isArray();
        }

        @Override
        public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) throws ParseException {
            Class<?> componentTypeClass = parameterClass.getComponentType();
            return new Gson().fromJson(data.toString(), Array.newInstance(componentTypeClass, 0).getClass());
        }
    }

    private static class ObjectTypeConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
            return !parameterClass.isPrimitive() && !parameterClass.isArray();
        }

        @Override
        public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) throws ParseException {
            if (data == null) return null;
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            if (parameterIndex < genericParameterTypes.length) {
                Type genericParameterType = genericParameterTypes[parameterIndex];
                if (genericParameterType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    Type type = TypeToken.getParameterized(parameterClass, actualTypeArguments).getType();
                    return new Gson().fromJson(data.toString(), type);
                }
            }
            return new Gson().fromJson(data.toString(), parameterClass);
        }
    }

    private static class MapParameterConverter implements ParameterConverter {
        @Override
        public boolean canSupport(Method method, int parameterIndex, Class<?> parameterClass, Object value) {
            return Map.class.isAssignableFrom(parameterClass);
        }

        @Override
        public Object converter(Method method, int parameterIndex, Class<?> parameterClass, Object data) throws ParseException {
            if (data == null) return new HashMap<>();
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            return new Gson().fromJson(new Gson().toJson(data), type);
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
                            return new Gson().fromJson(data.toString(), listType);
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
                            return new Gson().fromJson(data.toString(), listType);
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

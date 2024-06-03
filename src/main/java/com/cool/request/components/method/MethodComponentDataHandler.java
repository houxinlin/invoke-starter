package com.cool.request.components.method;

import com.cool.request.components.ComponentDataHandler;
import com.cool.request.components.SpringBootStartInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MethodComponentDataHandler implements ComponentDataHandler, MethodComponentListener {
    private ApplicationContext applicationContext;
    private SpringBootStartInfo springBootStartInfo;

    private final ParameterConvertManager parameterConvertManager = new ParameterConvertManager();

    private final Map<String, Class<?>> knownClass = new HashMap<>();


    public MethodComponentDataHandler(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo) {
        this.applicationContext = applicationContext;
        this.springBootStartInfo = springBootStartInfo;
        knownClass.put("int", int.class);
        knownClass.put("long", long.class);
        knownClass.put("char", char.class);
        knownClass.put("byte", byte.class);
        knownClass.put("double", double.class);
        knownClass.put("float", float.class);
        knownClass.put("boolean", boolean.class);
    }

    @Override
    public String invokeMethod(RMICallMethod rmiCallMethod) throws RemoteException {
        try {
            Class<?> clazz = Class.forName(rmiCallMethod.getClassName(), false, ClassLoader.getSystemClassLoader());
            List<String> parameterTypes = rmiCallMethod.getParameterTypes();
            Class<?>[] parameterClassTypes = new Class[parameterTypes.size()];
            Object[] parameterValue = new Object[parameterTypes.size()];
            for (int i = 0; i < parameterTypes.size(); i++) {
                String stringParameter = parameterTypes.get(i);
                if (knownClass.containsKey(stringParameter)) {
                    parameterClassTypes[i] = knownClass.get(stringParameter);
                } else {
                    Class<?> aClass = Class.forName(stringParameter, false, ClassLoader.getSystemClassLoader());
                    parameterClassTypes[i] = aClass;
                }
            }
            Method method = ReflectionUtils.findMethod(clazz, rmiCallMethod.getMethodName(), parameterClassTypes);
            if (method == null) return null;

            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                String parameterName = parameter.getName();
                Object parameterStringValue = rmiCallMethod.getParameters().getOrDefault(parameterName, "");
                try {
                    Object convertValue = parameterConvertManager.getConvertValue(parameter.getType(), parameterStringValue);
                    parameterValue[i] = convertValue;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            System.out.println(rmiCallMethod.getClassName());
            Map<String, ?> beansOfType = applicationContext.getBeansOfType(clazz);
            beansOfType.forEach((BiConsumer<String, Object>) (s, object) -> {
                try {
                    method.invoke(object, parameterValue);
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                }
            });
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    @Override
    public void componentInit(ApplicationContext applicationContext) {

    }
}

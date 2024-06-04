package com.cool.request.components.method;

import com.cool.request.components.ComponentDataHandler;
import com.cool.request.components.SpringBootStartInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MethodComponentDataHandler implements ComponentDataHandler, MethodComponentListener {
    private ApplicationContext applicationContext;
    private SpringBootStartInfo springBootStartInfo;
    private static final Logger LOGGER = Logger.getLogger("CoolRequest Method Call");
    private final ParameterConvertManager parameterConvertManager = new ParameterConvertManager();

    private final Map<String, Class<?>> knownClass = new HashMap<>();


    public MethodComponentDataHandler(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo) {
        this.applicationContext = applicationContext;
        this.springBootStartInfo = springBootStartInfo;
        knownClass.put("int", int.class);
        knownClass.put("long", long.class);
        knownClass.put("char", char.class);
        knownClass.put("byte", byte.class);
        knownClass.put("short", short.class);
        knownClass.put("double", double.class);
        knownClass.put("float", float.class);
        knownClass.put("boolean", boolean.class);
        knownClass.put("java.lang.String", String.class);
    }

    @Override
    public String invokeMethod(RMICallMethod rmiCallMethod) throws RemoteException {
        try {
            return doInvoke(rmiCallMethod);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            return e.getMessage();
        }
    }

    private String doInvoke(RMICallMethod rmiCallMethod) throws Exception {
        try {
            LOGGER.info("call:" + rmiCallMethod.getMethodName());
            LOGGER.info("args:" + rmiCallMethod.getParameters());
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
            if (method == null) {
                String msg = "method not found:" + rmiCallMethod.getMethodName();
                throw new IllegalArgumentException(msg);
            }

            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                String parameterName = parameter.getName();
                Object parameterStringValue = rmiCallMethod.getParameters().getOrDefault(parameterName, "");
                try {
                    Object convertValue = parameterConvertManager.getConvertValue(method, i, parameter.getType(), parameterStringValue);
                    parameterValue[i] = convertValue;
                } catch (ParseException e) {
                    String msg = "Cannot convert parameters:[" + parameterName + "] to type:" + parameter.getType();
                    throw new IllegalArgumentException(msg);
                }
            }
            return invoke(getObject(method), method, parameterValue);
        } catch (Exception e) {
            throw e;
        }
    }

    private String invoke(Object instance, Method method, Object[] parameterValue) {
        try {
            method.setAccessible(true);
            Object invokeResult = method.invoke(instance, parameterValue);
            if (void.class.equals(method.getReturnType()) || Void.class.equals(method.getReturnType())) {
                return "void";
            }
            return getReturnValueFromSpringMvc(invokeResult);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private Object getObject(Method method) throws NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException {
        if (Modifier.isStatic(method.getModifiers())) {
            return null;
        }
        Class<?> declaringClass = method.getDeclaringClass();
        Map<String, ?> beansOfType = applicationContext.getBeansOfType(declaringClass);
        if (beansOfType.isEmpty())
            return declaringClass.getConstructor().newInstance();
        for (Object value : beansOfType.values()) {
            return value;
        }
        throw new IllegalArgumentException("Unable to find instance in Spring container");
    }

    private String getReturnValueFromSpringMvc(Object invokeResult) {
        try {
            return springBootStartInfo.getJsonMapper().toJSONString(invokeResult);
        } catch (Exception ignored) {
        }
        return String.valueOf(invokeResult);
    }

    @Override
    public void componentInit(ApplicationContext applicationContext) {

    }
}

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MethodComponentDataHandler implements ComponentDataHandler, MethodComponentListener {
    private ApplicationContext applicationContext;
    private SpringBootStartInfo springBootStartInfo;
    private static final Logger LOGGER = Logger.getLogger("CoolRequest Method Call");
    private final ParameterConvertManager parameterConvertManager;

    private final Map<String, Class<?>> knownClass = new HashMap<>();


    public MethodComponentDataHandler(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo) {
        this.applicationContext = applicationContext;
        this.springBootStartInfo = springBootStartInfo;
        this.parameterConvertManager = new ParameterConvertManager(springBootStartInfo);
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
            e.printStackTrace();
            return e.getMessage();
        }
    }

    private String doInvoke(RMICallMethod rmiCallMethod) throws Exception {
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
            Object parameterStringValue = rmiCallMethod.getParameters().getOrDefault(String.valueOf(i), "");
            try {
                Object convertValue = parameterConvertManager.getConvertValue(method, i, parameter.getType(), parameterStringValue);
                parameterValue[i] = convertValue;
            } catch (Exception e) {
                String msg = "Cannot convert parameters:[" + parameterName + "] to type:" + parameter.getType();
                throw new IllegalArgumentException(msg);
            }
        }
        return invoke(getObject(method), method, parameterValue);
    }

    private String invoke(Object instance, Method method, Object[] parameterValue) throws Exception {
        try {
            method.setAccessible(true);
            Object invokeResult = method.invoke(instance, parameterValue);
            if (void.class.equals(method.getReturnType()) || Void.class.equals(method.getReturnType())) {
                return "void";
            }
            return getReturnValueFromSpringMvc(invokeResult);
        } catch (Exception e) {
            throw e;
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
        try {
            Map<String, ?> beansOfType = applicationContext.getBeansOfType(declaringClass);
            if (beansOfType.isEmpty())
                return declaringClass.getConstructor().newInstance();
            for (Object value : beansOfType.values()) {
                return value;
            }
        } catch (Exception ignored) {
        }
        return declaringClass.getConstructor().newInstance();
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

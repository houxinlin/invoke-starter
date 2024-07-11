package com.cool.request.components.method;

import com.cool.request.components.ComponentDataHandler;
import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.rmi.starter.CallResult;
import com.cool.request.utils.exception.ObjectNotFound;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;

public class MethodComponentDataHandler implements ComponentDataHandler, MethodComponentListener {
    private final ApplicationContext applicationContext;
    private final SpringBootStartInfo springBootStartInfo;
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
    public CallResult invokeMethod(RMICallMethod rmiCallMethod, int hasCode) {
        try {
            return doInvoke(rmiCallMethod, hasCode);
        } catch (Exception e) {
            e.printStackTrace();
            return new CallResult(true, true, e.getMessage());
        }
    }

    @Override
    public List<Integer> getHasCode(RMICallMethod rmiCallMethod) throws RemoteException {
        try {
            Class<?> clazz = Class.forName(rmiCallMethod.getClassName(), false, ClassLoader.getSystemClassLoader());
            try {
                Map<String, ?> beansOfType = applicationContext.getBeansOfType(clazz);
                if (beansOfType.isEmpty())
                    return new ArrayList<>();
                List<Integer> result = new ArrayList<>();
                for (Object value : beansOfType.values()) {
                    result.add((value.hashCode()));
                }
                return result;
            } catch (Exception ignored) {
            }

        } catch (ClassNotFoundException ignored) {
        }
        return Collections.emptyList();
    }

    private CallResult doInvoke(RMICallMethod rmiCallMethod, int hasCode) throws Exception {
        LOGGER.info("call:" + rmiCallMethod.getClassName() + "." + rmiCallMethod.getMethodName());
        LOGGER.info("args:" + rmiCallMethod.getParameters());
        Class<?> clazz = null;
        try {
            clazz = Class.forName(rmiCallMethod.getClassName(), false, ClassLoader.getSystemClassLoader());
        } catch (Exception e) {
            return new CallResult(false, false, e.getMessage());
        }
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
        try {
            Object object = getObject(method, hasCode);
            return invoke(object, method, parameterValue);
        } catch (ObjectNotFound object) {
            return new CallResult(false, false, "");
        }

    }

    private CallResult invoke(Object instance, Method method, Object[] parameterValue) throws Exception {
        try {
            method.setAccessible(true);
            Object invokeResult = method.invoke(instance, parameterValue);
            if (void.class.equals(method.getReturnType()) || Void.class.equals(method.getReturnType())) {
                return new CallResult(true, false, "void");
            }
            return getReturnValueFromSpringMvc(invokeResult);
        } catch (Exception e) {
            throw e;
        }
    }

    private Object getObject(Method method, int hasCode) throws Exception {
        if (Modifier.isStatic(method.getModifiers())) {
            return null;
        }
        Class<?> declaringClass = method.getDeclaringClass();
        if (hasCode == -1) {
            //如果不是bean，则尝试手动实例一下
            return declaringClass.getConstructor().newInstance();
        }
        Map<String, ?> beansOfType = applicationContext.getBeansOfType(declaringClass);
        if (beansOfType.isEmpty())
            return declaringClass.getConstructor().newInstance();
        for (Object value : beansOfType.values()) {
            if (hasCode == Objects.hashCode(value)) {
                return value;
            }
        }
        //指名了hasCode，但是没有找到，让插件进行下一个
        throw new ObjectNotFound();
    }

    private CallResult getReturnValueFromSpringMvc(Object invokeResult) {
        try {
            String jsonString = springBootStartInfo.getJsonMapper().toJSONString(invokeResult);
            return new CallResult(true, false, jsonString);
        } catch (Exception ignored) {
        }
        return new CallResult(true, false, String.valueOf(invokeResult));
    }

    @Override
    public void componentInit(ApplicationContext applicationContext) {

    }
}

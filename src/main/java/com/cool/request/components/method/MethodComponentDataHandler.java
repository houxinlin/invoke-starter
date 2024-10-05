package com.cool.request.components.method;

import com.cool.request.components.ComponentDataHandler;
import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.rmi.starter.CallResult;
import com.cool.request.utils.exception.ObjectNotFound;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
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
    public CallResult invokeMethod(RMICallMethod rmiCallMethod, int hasCode, byte[] code) {
        try {
            return doInvoke(rmiCallMethod, hasCode, code);
        } catch (Exception e) {
            e.printStackTrace();
            return new CallResult(true, true, e.getMessage());
        }
    }

    private Class<?> getClassFromClassLoader(RMICallMethod rmiCallMethod) throws ClassNotFoundException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader == null) {
            return Class.forName(rmiCallMethod.getClassName());
        }
        return Class.forName(rmiCallMethod.getClassName(), false, contextClassLoader);
    }

    private ClassLoader getClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader == null) {
            return ClassLoader.getSystemClassLoader();
        }
        return contextClassLoader;
    }

    @Override
    public List<Integer> getHasCode(RMICallMethod rmiCallMethod) {
        try {
            Map<String, ?> beansOfType = applicationContext.getBeansOfType(getClassFromClassLoader(rmiCallMethod));
            if (beansOfType.isEmpty())
                return new ArrayList<>();
            List<Integer> result = new ArrayList<>();
            for (Object value : beansOfType.values()) {
                result.add((value.hashCode()));
            }
            return result;
        } catch (Exception ignored) {
        }

        return Collections.emptyList();
    }

    private void invokeCallMethod(Object callMethodScriptObject, String methodName) {
        try {
            Method beforeCallMethod = callMethodScriptObject.getClass().getMethod(methodName, ApplicationContext.class);
            beforeCallMethod.invoke(callMethodScriptObject, applicationContext);
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object invokeParameterProcessor(Object callMethodScriptObject, Parameter parameter, int index, Object value) {
        try {
            Method beforeCallMethod = callMethodScriptObject.getClass()
                    .getMethod("parameterProcessor", ApplicationContext.class, Parameter.class, int.class, Object.class);
            return beforeCallMethod.invoke(callMethodScriptObject, applicationContext, parameter, index, value);
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value;
    }

    private CallResult doInvoke(RMICallMethod rmiCallMethod, int hasCode, byte[] scriptCode) throws Exception {
        LOGGER.info("call:" + rmiCallMethod.getClassName() + "." + rmiCallMethod.getMethodName());
        LOGGER.info("args:" + rmiCallMethod.getParameters());
        CallMethodClassLoader callMethodClassLoader;
        Object callMethodScriptObject = null;
        if (scriptCode != null) {
            callMethodClassLoader = new CallMethodClassLoader(scriptCode);
            Class<?> callMethodScriptClass = callMethodClassLoader.loadClass(CallMethodClassLoader.CLASS_NAME_METHOD_CALL);
            callMethodScriptObject = callMethodScriptClass.newInstance();
        }
        Class<?> clazz = null;
        try {
            clazz = getClassFromClassLoader(rmiCallMethod);
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
                Class<?> aClass = Class.forName(stringParameter, false, getClassLoader());
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
                if (callMethodScriptObject != null) {
                    parameterValue[i] = invokeParameterProcessor(callMethodScriptObject, parameter, i, convertValue);
                } else {
                    parameterValue[i] = convertValue;
                }
            } catch (Exception e) {
                String msg = "Cannot convert parameters:[" + parameterName + "] to type:" + parameter.getType();
                throw new IllegalArgumentException(msg);
            }
        }
        try {
            Object object = getObject(method, hasCode);
            if (callMethodScriptObject != null) {
                invokeCallMethod(callMethodScriptObject, "beforeCall");
            }
            CallResult invoke = invoke(object, method, parameterValue);
            if (callMethodScriptObject != null) {
                invokeCallMethod(callMethodScriptObject, "afterCall");
            }
            return invoke;
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
    public void invokeTestMethod(String className, String methodName, String classRoot, String... methodClassName) throws RemoteException {
        File directory = new File(classRoot);
        try {
            URL url = directory.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
            Class<?> loadedClass = classLoader.loadClass(className);
            Object bean = null;

            try {
                bean = applicationContext.getAutowireCapableBeanFactory().createBean(loadedClass);
                Class<?>[] parameterTypes = new Class[methodClassName.length];
                for (int i = 0; i < methodClassName.length; i++) {
                    parameterTypes[i] = Class.forName(methodClassName[i]);
                }
                Method method = ReflectionUtils.findMethod(loadedClass, methodName, parameterTypes);
                if (method == null) {
                    LOGGER.info("method not found:" + methodName);
                    return;
                }
                method.setAccessible(true);
                if (method.getParameterCount() > 0) {
                    method.invoke(bean, resolveMethodArguments(method, bean, bean.getClass().getSimpleName()));
                } else {
                    method.invoke(bean);
                }
            } finally {
                if (bean != null) {
                    applicationContext.getAutowireCapableBeanFactory().destroyBean(bean);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Object[] resolveMethodArguments(Method method, Object bean, @Nullable String beanName) {
        int argumentCount = method.getParameterCount();
        Object[] arguments = new Object[argumentCount];
        Set<String> autowiredBeans = new LinkedHashSet<>(argumentCount);
        if (applicationContext instanceof ConfigurableApplicationContext) {
            TypeConverter typeConverter = ((ConfigurableApplicationContext) applicationContext).getBeanFactory().getTypeConverter();
            for (int i = 0; i < arguments.length; i++) {
                MethodParameter methodParam = new MethodParameter(method, i);
                DependencyDescriptor currDesc = new DependencyDescriptor(methodParam, true);
                currDesc.setContainingClass(bean.getClass());
                try {
                    Object arg = ((ConfigurableApplicationContext) applicationContext).getBeanFactory().resolveDependency(currDesc, beanName, autowiredBeans, typeConverter);
                    arguments[i] = arg;
                } catch (BeansException ex) {
                    throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(methodParam), ex);
                }
            }
            return arguments;
        }
        throw new IllegalArgumentException();

    }

    @Override
    public void componentInit(ApplicationContext applicationContext) {

    }
}

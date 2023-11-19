package com.hxl.plugin.scheduledinvokestarter;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPart;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class VersionInstance {

    private static Class<?> loadPartClass() {
        try {
            if (VersionUtils.isSpringBoot3Dot0()) {
                return Class.forName("jakarta.servlet.http.Part");
            }
            return Class.forName("javax.servlet.http.Part");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private static Class<?> loadHttpServletRequestClass() {
        try {
            if (VersionUtils.isSpringBoot3Dot0()) {
                return Class.forName("jakarta.servlet.http.HttpServletRequest");
            }
            return Class.forName("javax.servlet.http.HttpServletRequest");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> loadHttpServletResponse() {
        try {
            if (VersionUtils.isSpringBoot3Dot0()) {
                return Class.forName("jakarta.servlet.http.HttpServletResponse");
            }
            return Class.forName("javax.servlet.http.HttpServletResponse");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean invokeHandlerInterceptor_preHandle(HandlerInterceptor instance, Object request, Object response, Object handler) {
        try {
            MethodType methodType = MethodType.methodType(boolean.class, loadHttpServletRequestClass(),loadHttpServletResponse(),Object.class);
            MethodHandle handle = MethodHandles.lookup().findVirtual(Class.forName("org.springframework.web.servlet.HandlerInterceptor"), "preHandle", methodType);
            return (boolean) handle.bindTo(instance).invokeWithArguments(request,response,handler);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean invokeHandlerInterceptor_afterCompletion(HandlerInterceptor instance, Object request, Object response, Object handler, Exception exception) {
        try {
            MethodType methodType = MethodType.methodType(boolean.class, loadHttpServletRequestClass(),loadHttpServletResponse(),Object.class,Exception.class);
            MethodHandle handle = MethodHandles.lookup().findVirtual(Class.forName("org.springframework.web.servlet.HandlerInterceptor"), "afterCompletion", methodType);
            return (boolean) handle.bindTo(instance).invokeWithArguments(request,response,handler,exception);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void invokeHandlerInterceptor_postHandle(HandlerInterceptor instance, Object request, Object response, Object handler, ModelAndView modelAndView) {
        try {
            MethodType methodType = MethodType.methodType(void.class, loadHttpServletRequestClass(),loadHttpServletResponse(),Object.class,ModelAndView.class);
            MethodHandle handle = MethodHandles.lookup().findVirtual(Class.forName("org.springframework.web.servlet.HandlerInterceptor"), "postHandle", methodType);
            handle.bindTo(instance).invokeWithArguments(request,response,handler,modelAndView);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static HandlerExecutionChain invokeHandlerMapping_getHandler(HandlerMapping mapping, Object request) {
        try {
            Class<?> handlerExecutionChainClass = Class.forName("org.springframework.web.servlet.HandlerExecutionChain");
            MethodType methodType = MethodType.methodType(handlerExecutionChainClass, loadHttpServletRequestClass());
            MethodHandle getHandler = MethodHandles.lookup().findVirtual(Class.forName("org.springframework.web.servlet.HandlerMapping"), "getHandler", methodType);
            return (HandlerExecutionChain) getHandler.invoke(mapping, request);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public static void invokeServletRequestPathUtils_parseAndCache(Object httpRequestServlet) {
        try {
            MethodType methodType = MethodType.methodType(Class.forName("org.springframework.http.server.RequestPath"), loadHttpServletRequestClass());
            MethodHandles.lookup()
                    .findStatic(Class.forName("org.springframework.web.util.ServletRequestPathUtils"), "parseAndCache", methodType)
                    .invoke(httpRequestServlet);
        } catch (Throwable e) {
        }
    }

    public static ServletRequestAttributes newServletRequestAttributes(Object mockHttpServletRequest) {
        try {
            Class<?> aClass = Class.forName("org.springframework.web.context.request.ServletRequestAttributes");
            Constructor<?> constructor = aClass.getDeclaredConstructor(loadHttpServletRequestClass());
            return (ServletRequestAttributes) constructor.newInstance(mockHttpServletRequest);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static ModelAndView invokeHandlerAdapter_handle(HandlerAdapter instance, Object request, Object response, Object handler) {
        try {
            MethodType methodType = MethodType.methodType(ModelAndView.class, loadHttpServletRequestClass(),loadHttpServletResponse(),Object.class);
            MethodHandle handle = MethodHandles.lookup().findVirtual(Class.forName("org.springframework.web.servlet.HandlerAdapter"), "handle", methodType);
            return (ModelAndView) handle.bindTo(instance).invokeWithArguments(request,response,handler);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void invokeHttpServletRequest_addPart(Object instance,Object arg) {
        try {

            MethodType methodType = MethodType.methodType(void.class, loadPartClass());
            MethodHandle addPartHandle = MethodHandles.lookup().findVirtual(Class.forName("org.springframework.mock.web.MockHttpServletRequest"), "addPart", methodType);
            addPartHandle.bindTo(instance).invokeWithArguments(arg);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


}

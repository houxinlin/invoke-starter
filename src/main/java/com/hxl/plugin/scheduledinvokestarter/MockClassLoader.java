package com.hxl.plugin.scheduledinvokestarter;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


public class MockClassLoader extends URLClassLoader {
    public MockClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }


//    static {
//        try {
//            classLoader = VersionUtils.isSpringBoot3Dot0() ? new URLClassLoader(new URL[]{new File(Paths.get(Config.getLibPath(), Config.SPRING_TEST_6).toString()).toURI().toURL()}) :
//                    new URLClassLoader(new URL[]{new File(Paths.get(Config.getLibPath(), Config.SPRING_TEST_5).toString()).toURI().toURL()},ClassLoader.getSystemClassLoader());
//        } catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public MockHttpServletRequest loadMockHttpServletRequest() {
        return (MockHttpServletRequest) createInstance("org.springframework.mock.web.MockHttpServletRequest");
    }

    public MockHttpServletRequest loadMockMultipartHttpServletRequest() {
        return (MockHttpServletRequest) createInstance("org.springframework.mock.web.MockMultipartHttpServletRequest");
    }

    public MockHttpServletResponse loadMockHttpServletResponse() {
        return (MockHttpServletResponse) createInstance("org.springframework.mock.web.MockHttpServletResponse");
    }

    private Object createInstance(String clName) {
        try {
            return loadClass(clName).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public MockPart loadMockPart(String name, byte[] value) {
        try {
            return (MockPart) loadClass("org.springframework.mock.web.MockPart").getConstructor(String.class, byte[].class).newInstance(name, value);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public MockMultipartFile loadMockMultipartFile(String name, byte[] value) {
        try {
            return (MockMultipartFile) loadClass("org.springframework.mock.web.MockMultipartFile").getConstructor(String.class, byte[].class).newInstance(name, value);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            if (name.startsWith("com.hxl.plugin.scheduledinvokestarter")){
                InputStream resourceAsStream = getParent().getResourceAsStream(name.replace(".", "/") + ".class");
                byte[] bytes = StreamUtils.copyToByteArray(resourceAsStream);
                return defineClass(name,bytes,0,bytes.length);
            }
            return findClass(name);
        } catch (Exception ignored) {
        }
        return super.loadClass(name, resolve);
    }
}

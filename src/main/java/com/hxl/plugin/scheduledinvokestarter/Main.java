package com.hxl.plugin.scheduledinvokestarter;

import org.springframework.util.StreamUtils;

import javax.servlet.RequestDispatcher;

public class Main {
    public static void main(String[] args) {
        try {
            Class<?> aClass = MockClassLoader.newMockClassLoader().loadClass("org.springframework.mock.web.MockHttpServletRequest");

            Object o = aClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }
}

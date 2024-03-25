package com.cool.request;

import java.lang.reflect.Method;

public class ScheduledEndpoint {
    private Method method;
    private Object bean;

    public ScheduledEndpoint(Method method, Object bean) {
        this.method = method;
        this.bean = bean;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }
}

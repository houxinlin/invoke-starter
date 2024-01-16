package com.hxl.plugin.scheduledinvokestarter.model;

import java.util.List;
import java.util.Objects;

public class SpringMvcRequestMappingSpringInvokeEndpoint extends SpringInvokeEndpoint {
    private String url;
    private String simpleClassName;
    private String methodName;
    private String httpMethod;

    private List<String> paramClassList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpringMvcRequestMappingSpringInvokeEndpoint)) return false;
        SpringMvcRequestMappingSpringInvokeEndpoint that = (SpringMvcRequestMappingSpringInvokeEndpoint) o;
        return Objects.equals(getUrl(), that.getUrl()) && Objects.equals(getSimpleClassName(), that.getSimpleClassName()) && Objects.equals(getMethodName(), that.getMethodName()) && Objects.equals(getHttpMethod(), that.getHttpMethod());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUrl(), getSimpleClassName(), getMethodName(), getHttpMethod());
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }

    public void setSimpleClassName(String simpleClassName) {
        this.simpleClassName = simpleClassName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }


    public List<String> getParamClassList() {
        return paramClassList;
    }

    public void setParamClassList(List<String> paramClassList) {
        this.paramClassList = paramClassList;
    }

    public static final class RequestMappingInvokeBeanBuilder {
        private String id;
        private String url;
        private String simpleClassName;
        private String methodName;
        private String httpMethod;
        private List<String> paramClassList;

        private RequestMappingInvokeBeanBuilder() {
        }

        public static RequestMappingInvokeBeanBuilder aRequestMappingInvokeBean() {
            return new RequestMappingInvokeBeanBuilder();
        }

        public RequestMappingInvokeBeanBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public RequestMappingInvokeBeanBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public RequestMappingInvokeBeanBuilder withSimpleClassName(String simpleClassName) {
            this.simpleClassName = simpleClassName;
            return this;
        }

        public RequestMappingInvokeBeanBuilder withMethodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public RequestMappingInvokeBeanBuilder withHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public SpringMvcRequestMappingSpringInvokeEndpoint build() {
            SpringMvcRequestMappingSpringInvokeEndpoint springMvcRequestMappingInvokeBean = new SpringMvcRequestMappingSpringInvokeEndpoint();
            springMvcRequestMappingInvokeBean.setId(id);
            springMvcRequestMappingInvokeBean.setUrl(url);
            springMvcRequestMappingInvokeBean.setSimpleClassName(simpleClassName);
            springMvcRequestMappingInvokeBean.setMethodName(methodName);
            springMvcRequestMappingInvokeBean.setHttpMethod(httpMethod);
            return springMvcRequestMappingInvokeBean;
        }
    }
}

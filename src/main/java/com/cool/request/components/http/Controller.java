package com.cool.request.components.http;


import java.io.Serializable;
import java.util.List;

public class Controller implements Serializable {
    private static final long serialVersionUID = 1000000000;
    private String moduleName;
    private String contextPath;
    private int serverPort;
    private String url;
    private String simpleClassName;
    private String methodName;
    private String httpMethod;
    private List<String> paramClassList;
    private String springInnerId;

    public String getSpringInnerId() {
        return springInnerId;
    }

    public void setSpringInnerId(String springInnerId) {
        this.springInnerId = springInnerId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
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

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public List<String> getParamClassList() {
        return paramClassList;
    }

    public void setParamClassList(List<String> paramClassList) {
        this.paramClassList = paramClassList;
    }


    public static final class ControllerBuilder {
        private String moduleName;
        private String contextPath;
        private int serverPort;
        private String url;
        private String simpleClassName;
        private String methodName;
        private String httpMethod;
        private List<String> paramClassList;

        private ControllerBuilder() {
        }

        public static ControllerBuilder aController() {
            return new ControllerBuilder();
        }

        public ControllerBuilder withModuleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }

        public ControllerBuilder withContextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public ControllerBuilder withServerPort(int serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public ControllerBuilder withUrl(String url) {
            this.url = url;
            return this;
        }

        public ControllerBuilder withSimpleClassName(String simpleClassName) {
            this.simpleClassName = simpleClassName;
            return this;
        }

        public ControllerBuilder withMethodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public ControllerBuilder withHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public ControllerBuilder withParamClassList(List<String> paramClassList) {
            this.paramClassList = paramClassList;
            return this;
        }

        public Controller build(Controller controller) {
            controller.setModuleName(moduleName);
            controller.setContextPath(contextPath);
            controller.setServerPort(serverPort);
            controller.setUrl(url);
            controller.setSimpleClassName(simpleClassName);
            controller.setMethodName(methodName);
            controller.setHttpMethod(httpMethod);
            controller.setParamClassList(paramClassList);
            return controller;
        }
    }
}

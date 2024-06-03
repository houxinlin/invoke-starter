package com.cool.request.components.method;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class RMICallMethod implements Serializable {
    private static final long serialVersionUID = 1000000000L;
    private String className;

    private String methodName;
    private Map<String, Object> parameters;

    private List<String> parameterTypes;

    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}

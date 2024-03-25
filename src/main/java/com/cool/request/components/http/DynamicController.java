package com.cool.request.components.http;

import java.util.ArrayList;

public class DynamicController extends Controller {
    private static final long serialVersionUID = 1000000000;
    private int springBootStartPort = 0;
    private String springInnerId;

    public DynamicController() {
    }

    public void setSpringBootStartPort(int springBootStartPort) {
        this.springBootStartPort = springBootStartPort;
    }

    public DynamicController(Controller controller) {
        copyProperties(controller, this);
    }

    private void copyProperties(Controller source, DynamicController target) {
        target.setModuleName(source.getModuleName());
        target.setContextPath(source.getContextPath());
        target.setServerPort(source.getServerPort());
        target.setUrl(source.getUrl());
        target.setSimpleClassName(source.getSimpleClassName());
        target.setMethodName(source.getMethodName());
        target.setHttpMethod(source.getHttpMethod());
        target.setParamClassList(new ArrayList<>(source.getParamClassList()));
    }


    public String getSpringInnerId() {
        return springInnerId;
    }

    public void setSpringInnerId(String springInnerId) {
        this.springInnerId = springInnerId;
    }
}

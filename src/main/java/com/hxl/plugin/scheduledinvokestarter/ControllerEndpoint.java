package com.hxl.plugin.scheduledinvokestarter;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

public class ControllerEndpoint {
    private RequestMappingInfo requestMappingInfo;
    private HandlerMethod handlerMethod;

    public ControllerEndpoint(RequestMappingInfo requestMappingInfo, HandlerMethod handlerMethod) {
        this.requestMappingInfo = requestMappingInfo;
        this.handlerMethod = handlerMethod;
    }

    public HandlerMethod getHandlerMethod() {
        return handlerMethod;
    }

    public void setHandlerMethod(HandlerMethod handlerMethod) {
        this.handlerMethod = handlerMethod;
    }

    public RequestMappingInfo getRequestMappingInfo() {
        return requestMappingInfo;
    }

    public void setRequestMappingInfo(RequestMappingInfo requestMappingInfo) {
        this.requestMappingInfo = requestMappingInfo;
    }
}

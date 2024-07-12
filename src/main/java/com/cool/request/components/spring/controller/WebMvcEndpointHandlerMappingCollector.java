package com.cool.request.components.spring.controller;

import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.components.http.DynamicController;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebMvcEndpointHandlerMappingCollector extends RequestMappingCollector {
    @Override
    public List<DynamicController> collect(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo) {
        Map<String, WebMvcEndpointHandlerMapping> beansOfType = applicationContext.getBeansOfType(WebMvcEndpointHandlerMapping.class);
        List<DynamicController> result = new ArrayList<>();
        beansOfType.values().forEach(mapping -> {
            parseController(applicationContext, result, mapping.getHandlerMethods(), springBootStartInfo);
        });
        return result;
    }
}

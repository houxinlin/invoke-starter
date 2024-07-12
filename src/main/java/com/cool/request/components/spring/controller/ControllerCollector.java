package com.cool.request.components.spring.controller;

import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.components.http.DynamicController;
import org.springframework.context.ApplicationContext;

import java.util.List;

public interface ControllerCollector {
    public List<DynamicController> collect(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo);

}

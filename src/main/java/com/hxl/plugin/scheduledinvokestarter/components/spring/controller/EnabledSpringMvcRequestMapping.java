package com.hxl.plugin.scheduledinvokestarter.components.spring.controller;

import com.hxl.plugin.scheduledinvokestarter.components.ComponentDataHandler;
import com.hxl.plugin.scheduledinvokestarter.components.ComponentSupport;
import com.hxl.plugin.scheduledinvokestarter.components.SpringBootStartInfo;
import com.hxl.plugin.scheduledinvokestarter.json.JsonMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class EnabledSpringMvcRequestMapping implements ComponentSupport {
    private static final String className = "com.hxl.plugin.scheduledinvokestarter.components.spring.controller.SpringRequestMappingComponent";

    @Override
    public boolean canSupport(ApplicationContext applicationContext) {
        return applicationContext instanceof WebApplicationContext;
    }

    @Override
    public ComponentDataHandler start(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo, JsonMapper jsonMapper) {
        return (new SpringRequestMappingComponent(applicationContext, springBootStartInfo));

    }
}

package com.cool.request.components.method;

import com.cool.request.components.ComponentDataHandler;
import com.cool.request.components.ComponentSupport;
import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.json.JsonMapper;
import org.springframework.context.ApplicationContext;

public class MethodComponentSupport implements ComponentSupport {
    @Override
    public boolean canSupport(ApplicationContext applicationContext) {
        return true;
    }

    @Override
    public ComponentDataHandler start(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo, JsonMapper jsonMapper) {
        return new MethodComponentDataHandler(applicationContext, springBootStartInfo);
    }
}

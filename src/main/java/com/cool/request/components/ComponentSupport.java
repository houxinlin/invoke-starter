package com.cool.request.components;

import com.cool.request.json.JsonMapper;
import org.springframework.context.ApplicationContext;

public interface ComponentSupport {
    public boolean canSupport(ApplicationContext applicationContext);

    public ComponentDataHandler start(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo, JsonMapper jsonMapper);
}

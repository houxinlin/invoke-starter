package com.hxl.plugin.scheduledinvokestarter.components;

import com.hxl.plugin.scheduledinvokestarter.json.JsonMapper;
import org.springframework.context.ApplicationContext;

public interface ComponentSupport {
    public boolean canSupport(ApplicationContext applicationContext);

    public ComponentDataHandler start(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo, JsonMapper jsonMapper);
}

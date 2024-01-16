package com.hxl.plugin.scheduledinvokestarter.components;

import org.springframework.context.ApplicationContext;

public interface ComponentSupport {
    public boolean canSupport(ApplicationContext applicationContext);

    public ComponentDataHandler start(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo);
}

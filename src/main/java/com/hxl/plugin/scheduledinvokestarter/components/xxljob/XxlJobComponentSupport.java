package com.hxl.plugin.scheduledinvokestarter.components.xxljob;

import com.hxl.plugin.scheduledinvokestarter.components.ComponentDataHandler;
import com.hxl.plugin.scheduledinvokestarter.components.ComponentSupport;
import com.hxl.plugin.scheduledinvokestarter.components.SpringBootStartInfo;
import com.hxl.plugin.scheduledinvokestarter.json.JsonMapper;
import org.springframework.context.ApplicationContext;

public class XxlJobComponentSupport implements ComponentSupport {
    @Override
    public boolean canSupport(ApplicationContext applicationContext) {
        try {
            Class.forName("com.xxl.job.core.context.XxlJobHelper");
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    @Override
    public ComponentDataHandler start(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo, JsonMapper jsonMapper) {
        return new XXJJobComponentDataHandler(jsonMapper,applicationContext,springBootStartInfo);
    }
}

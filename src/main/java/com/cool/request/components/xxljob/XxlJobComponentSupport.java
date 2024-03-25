package com.cool.request.components.xxljob;

import com.cool.request.CoolRequestProjectLog;
import com.cool.request.components.ComponentDataHandler;
import com.cool.request.components.ComponentSupport;
import com.cool.request.components.SpringBootStartInfo;
import com.cool.request.json.JsonMapper;
import org.springframework.context.ApplicationContext;

public class XxlJobComponentSupport implements ComponentSupport {
    @Override
    public boolean canSupport(ApplicationContext applicationContext) {
        try {
            Class.forName("com.xxl.job.core.context.XxlJobHelper");
            CoolRequestProjectLog.log("XXL-JOB组件启动");
            return true;
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    @Override
    public ComponentDataHandler start(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo, JsonMapper jsonMapper) {
        return new XXJJobComponentDataHandler(applicationContext, springBootStartInfo);
    }
}

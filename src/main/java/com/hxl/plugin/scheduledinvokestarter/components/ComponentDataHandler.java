package com.hxl.plugin.scheduledinvokestarter.components;

import org.springframework.context.ApplicationContext;

public interface ComponentDataHandler {
    public void publishData(ApplicationContext applicationContext);

    public void messageData(String msg);
}

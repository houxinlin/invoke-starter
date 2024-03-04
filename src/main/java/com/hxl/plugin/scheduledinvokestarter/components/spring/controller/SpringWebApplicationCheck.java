package com.hxl.plugin.scheduledinvokestarter.components.spring.controller;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class SpringWebApplicationCheck {
    public static boolean check(ApplicationContext applicationContext) {
        return applicationContext instanceof WebApplicationContext;
    }

}

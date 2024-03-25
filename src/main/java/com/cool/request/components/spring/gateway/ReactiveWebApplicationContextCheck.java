package com.cool.request.components.spring.gateway;

import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext;
import org.springframework.context.ApplicationContext;

public class ReactiveWebApplicationContextCheck {
    public static boolean check(ApplicationContext applicationContext) {
        return applicationContext instanceof ReactiveWebApplicationContext;
    }

}

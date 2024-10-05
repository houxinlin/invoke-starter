package com.cool.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;

public class CoolRequestStarterApplication implements SpringApplicationRunListener {
    private static SpringApplication application;

    public CoolRequestStarterApplication(SpringApplication application, String[] args) {
        this.application = application;
    }

    public static SpringApplication getApplication() {
        return application;
    }
}

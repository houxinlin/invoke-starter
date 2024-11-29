package com.cool.request;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.time.Duration;

public class CoolRequestStarterApplication implements SpringApplicationRunListener {
    private static SpringApplication application;

    public CoolRequestStarterApplication(SpringApplication application, String[] args) {
        this.application = application;
    }

    public static SpringApplication getApplication() {
        return application;
    }

    public void starting() {

    }

    public void environmentPrepared(ConfigurableEnvironment environment) {

    }

    public void finished(ConfigurableApplicationContext context, Throwable exception) {

    }

    public void starting(ConfigurableBootstrapContext bootstrapContext) {
    }

    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
    }

    public void contextPrepared(ConfigurableApplicationContext context) {
    }

    public void contextLoaded(ConfigurableApplicationContext context) {
    }

    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
    }

    public void started(){

    }
    public void started(ConfigurableApplicationContext context) {
    }

    public void ready(ConfigurableApplicationContext context, Duration timeTaken) {
    }

    public void running(ConfigurableApplicationContext context) {
    }

    public void failed(ConfigurableApplicationContext context, Throwable exception) {
    }
}

package com.hxl.plugin.scheduledinvokestarter.model;

import com.hxl.plugin.scheduledinvokestarter.components.spring.controller.data.Controller;

import java.util.Set;

public class RequestMappingModel  extends Model{
    private int pluginPort;
    private Set<Controller> controllers;
    private int serverPort;

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getPluginPort() {
        return pluginPort;
    }

    public void setPluginPort(int pluginPort) {
        this.pluginPort = pluginPort;
    }
    public Set<Controller> getControllers() {
        return controllers;
    }

    public void setControllers(Set<Controller> controllers) {
        this.controllers = controllers;
    }


}

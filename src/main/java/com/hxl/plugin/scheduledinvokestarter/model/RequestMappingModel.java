package com.hxl.plugin.scheduledinvokestarter.model;

import com.hxl.plugin.scheduledinvokestarter.components.spring.controller.data.Controller;

import java.util.Set;

public class RequestMappingModel extends Model {
    private int pluginPort;
    private Controller controller;
    private int serverPort;
    private int total;

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

    public Controller getController() {
        return controller;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}

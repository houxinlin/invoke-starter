package com.hxl.plugin.scheduledinvokestarter.model;

public class ProjectStartupModel  extends Model{
    public ProjectStartupModel(int port) {
        this.port = port;
    }
    private int projectPort;

    public int getProjectPort() {
        return projectPort;
    }

    public void setProjectPort(int projectPort) {
        this.projectPort = projectPort;
    }

    public int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

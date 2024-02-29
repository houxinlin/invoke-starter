package com.hxl.plugin.scheduledinvokestarter.model;

import java.util.List;

public class XxlModel extends Model {
    private List<XxlJobInvokeEndpoint> xxlJobInvokeEndpoint;
    private int serverPort = 0;

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public List<XxlJobInvokeEndpoint> getXxlJobInvokeEndpoint() {
        return xxlJobInvokeEndpoint;
    }

    public void setXxlJobInvokeEndpoint(List<XxlJobInvokeEndpoint> xxlJobInvokeEndpoint) {
        this.xxlJobInvokeEndpoint = xxlJobInvokeEndpoint;
    }
}

package com.hxl.plugin.scheduledinvokestarter.model;

import java.util.List;

public class XxlModel extends Model {
    private List<XxlJobInvokeEndpoint> xxlJobInvokeEndpoint;

    public List<XxlJobInvokeEndpoint> getXxlJobInvokeEndpoint() {
        return xxlJobInvokeEndpoint;
    }

    public void setXxlJobInvokeEndpoint(List<XxlJobInvokeEndpoint> xxlJobInvokeEndpoint) {
        this.xxlJobInvokeEndpoint = xxlJobInvokeEndpoint;
    }
}

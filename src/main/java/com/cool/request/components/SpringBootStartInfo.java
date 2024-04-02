package com.cool.request.components;

import com.cool.request.json.JsonMapper;
import com.cool.request.rmi.plugin.ICoolRequestPluginRMI;
import com.cool.request.rmi.starter.ICoolRequestStarterRMI;

public class SpringBootStartInfo {
    private int availableTcpPort;
    private ICoolRequestPluginRMI coolRequestPluginRMI;
    private ICoolRequestStarterRMI coolRequestStarterRMI;
    public ICoolRequestPluginRMI getCoolRequestPluginRMI() {
        return coolRequestPluginRMI;
    }

    public void setCoolRequestPluginRMI(ICoolRequestPluginRMI coolRequestPluginRMI) {
        this.coolRequestPluginRMI = coolRequestPluginRMI;
    }

    public ICoolRequestStarterRMI getCoolRequestStarterRMI() {
        return coolRequestStarterRMI;
    }

    public void setCoolRequestStarterRMI(ICoolRequestStarterRMI coolRequestStarterRMI) {
        this.coolRequestStarterRMI = coolRequestStarterRMI;
    }

    private JsonMapper jsonMapper;

    public JsonMapper getJsonMapper() {
        return jsonMapper;
    }

    public void setJsonMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public int getAvailableTcpPort() {
        return availableTcpPort;
    }

    public void setAvailableTcpPort(int availableTcpPort) {
        this.availableTcpPort = availableTcpPort;
    }
}

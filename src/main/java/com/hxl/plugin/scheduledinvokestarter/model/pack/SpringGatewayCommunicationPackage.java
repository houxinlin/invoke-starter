package com.hxl.plugin.scheduledinvokestarter.model.pack;

import com.hxl.plugin.scheduledinvokestarter.model.Model;

public class SpringGatewayCommunicationPackage  extends CommunicationPackage{
    public SpringGatewayCommunicationPackage(Model model) {
        super(model);
    }

    @Override
    public String getType() {
        return "spring_gateway";
    }
}

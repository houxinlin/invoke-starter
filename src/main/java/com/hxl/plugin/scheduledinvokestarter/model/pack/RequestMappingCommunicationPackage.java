package com.hxl.plugin.scheduledinvokestarter.model.pack;

import com.hxl.plugin.scheduledinvokestarter.model.Model;

public class RequestMappingCommunicationPackage extends CommunicationPackage {
    public RequestMappingCommunicationPackage(Model model) {
        super(model);
    }

    @Override
    public String getType() {
        return "controller";
    }

}

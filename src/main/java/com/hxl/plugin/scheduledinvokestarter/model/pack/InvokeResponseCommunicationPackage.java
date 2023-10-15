package com.hxl.plugin.scheduledinvokestarter.model.pack;

import com.hxl.plugin.scheduledinvokestarter.model.Model;

public class InvokeResponseCommunicationPackage  extends CommunicationPackage{
    public InvokeResponseCommunicationPackage(Model model) {
        super(model);
    }

    @Override
    public String getType() {
        return "response_info";
    }
}

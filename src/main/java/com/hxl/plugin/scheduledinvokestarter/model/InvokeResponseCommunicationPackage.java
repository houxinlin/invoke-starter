package com.hxl.plugin.scheduledinvokestarter.model;

public class InvokeResponseCommunicationPackage  extends CommunicationPackage{
    public InvokeResponseCommunicationPackage(Model model) {
        super(model);
    }

    @Override
    public String getType() {
        return "response_info";
    }
}

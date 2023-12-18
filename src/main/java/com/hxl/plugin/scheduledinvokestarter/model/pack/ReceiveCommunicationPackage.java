package com.hxl.plugin.scheduledinvokestarter.model.pack;

import com.hxl.plugin.scheduledinvokestarter.model.Model;

public class ReceiveCommunicationPackage extends CommunicationPackage{
    public ReceiveCommunicationPackage(Model model) {
        super(model);
    }

    @Override
    public String getType() {
        return "invoke_receive";
    }
}

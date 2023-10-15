package com.hxl.plugin.scheduledinvokestarter.model.pack;

import com.hxl.plugin.scheduledinvokestarter.model.Model;

public class ClearCommunicationPackage  extends CommunicationPackage{
    public ClearCommunicationPackage(Model model) {
        super(model);
    }
    @Override
    public String getType() {
        return "clear";
    }
}

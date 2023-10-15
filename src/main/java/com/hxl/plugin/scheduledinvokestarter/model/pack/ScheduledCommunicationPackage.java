package com.hxl.plugin.scheduledinvokestarter.model.pack;

import com.hxl.plugin.scheduledinvokestarter.model.Model;

public class ScheduledCommunicationPackage  extends CommunicationPackage{
    public ScheduledCommunicationPackage(Model model) {
        super(model);
    }


    @Override
    public String getType() {
        return "scheduled";
    }
}

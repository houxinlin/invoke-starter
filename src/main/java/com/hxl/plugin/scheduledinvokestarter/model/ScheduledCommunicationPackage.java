package com.hxl.plugin.scheduledinvokestarter.model;

public class ScheduledCommunicationPackage  extends CommunicationPackage{
    public ScheduledCommunicationPackage(Model model) {
        super(model);
    }


    @Override
    public String getType() {
        return "scheduled";
    }
}

package com.hxl.plugin.scheduledinvokestarter.model.pack;

import com.hxl.plugin.scheduledinvokestarter.model.ProjectStartupModel;

public class ProjectStartupCommunicationPackage  extends CommunicationPackage{
    public ProjectStartupCommunicationPackage(ProjectStartupModel model) {
        super(model);
    }
    @Override
    public String getType() {
        return "startup";
    }
}

package com.hxl.plugin.scheduledinvokestarter.model.pack;

import com.hxl.plugin.scheduledinvokestarter.model.Model;

public class XXLJobPackage extends CommunicationPackage {
    public XXLJobPackage(Model model) {
        super(model);
    }

    @Override
    public String getType() {
        return "xxl_job";
    }
}

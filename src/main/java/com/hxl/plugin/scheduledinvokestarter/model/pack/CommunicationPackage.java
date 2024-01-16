package com.hxl.plugin.scheduledinvokestarter.model.pack;

import com.hxl.plugin.scheduledinvokestarter.json.JsonMapper;
import com.hxl.plugin.scheduledinvokestarter.json.JsonMapperFactory;
import com.hxl.plugin.scheduledinvokestarter.model.Model;

public abstract class CommunicationPackage {
    private final Model data;

    public CommunicationPackage(Model model) {
        model.setType(getType());
        this.data = model;
    }

    public abstract String getType();

    public String toJson() {
        JsonMapper jsonMapper = JsonMapperFactory.getJsonMapper();
        if (jsonMapper != null) {
            return JsonMapperFactory.getJsonMapper().toJSONString(this.data);
        }
        return "";
    }
}

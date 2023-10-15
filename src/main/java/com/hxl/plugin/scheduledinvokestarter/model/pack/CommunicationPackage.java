package com.hxl.plugin.scheduledinvokestarter.model.pack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxl.plugin.scheduledinvokestarter.model.Model;

public abstract class CommunicationPackage {
    private final Model data;

    public CommunicationPackage(Model model) {
        model.setType(getType());
        this.data =model;
    }
    public abstract String getType();

    public String toJson() {
        if (data ==null) return "{}";
        return toJson(data);
    }
    public String toJson(Object o) {
        try {
            return new ObjectMapper().writeValueAsString(o);
        } catch (JsonProcessingException ignored) {
        }
        return "{}";
    }
}

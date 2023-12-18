package com.hxl.plugin.scheduledinvokestarter.json;

import java.util.Map;

public interface JsonMapper {
    public Map<String, Object> toMap(String json) throws JsonException;
    public String toJSONString(Object o);
}

package com.hxl.plugin.scheduledinvokestarter.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class GsonMapper  implements JsonMapper{
    private final Gson gson =new Gson();
    @Override
    public Map<String, Object> toMap(String json) {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        return gson.fromJson(json, type);
    }

    @Override
    public <T> T toBean(String json, Class<T> tClass) throws JsonException {
        return  gson.fromJson(json,tClass);
    }

    @Override
    public String toJSONString(Object o) {
        return gson.toJson(o);
    }
}

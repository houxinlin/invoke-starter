package com.cool.request.json;

import java.util.Map;

public interface JsonMapper {
    public Map<String, Object> toMap(String json) throws JsonException;

    public <T>  T toBean(String json,Class<T> tClass) throws JsonException;
    public String toJSONString(Object o);
}

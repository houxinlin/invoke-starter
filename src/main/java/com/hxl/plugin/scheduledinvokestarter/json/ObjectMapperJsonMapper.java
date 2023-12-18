package com.hxl.plugin.scheduledinvokestarter.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class ObjectMapperJsonMapper implements JsonMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> toMap(String json) throws JsonException {
        try {
            return objectMapper.readValue(json, new TypeReference<HashMap<String,Object>>() {});
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage());
        }
    }

    @Override
    public String toJSONString(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException ignored) {

        }
        return "{}";
    }
}

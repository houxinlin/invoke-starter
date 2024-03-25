package com.cool.request.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class ObjectMapperJsonMapper implements JsonMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ObjectMapperJsonMapper() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    @Override
    public Map<String, Object> toMap(String json) throws JsonException {
        try {
            return objectMapper.readValue(json, new TypeReference<HashMap<String,Object>>() {});
        } catch (JsonProcessingException e) {
            throw new JsonException(e.getMessage());
        }
    }

    @Override
    public <T> T toBean(String json, Class<T> tClass) throws JsonException {
        try {
            return objectMapper.readValue(json,tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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

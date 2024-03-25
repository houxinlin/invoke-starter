package com.cool.request.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.Map;

public class GsonMapper implements JsonMapper {
    private final Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
            new ByteArrayToBase64TypeAdapter()).create();

    @Override
    public Map<String, Object> toMap(String json) {
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    @Override
    public <T> T toBean(String json, Class<T> tClass) throws JsonException {
        return gson.fromJson(json, tClass);
    }

    @Override
    public String toJSONString(Object o) {
        return gson.toJson(o);
    }

    private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.getDecoder().decode(json.getAsString());
        }

        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
        }
    }
}

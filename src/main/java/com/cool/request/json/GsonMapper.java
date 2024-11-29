package com.cool.request.json;

import com.cool.request.utils.LocalDateTypeAdapter;
import com.cool.request.utils.LocalTimeTypeAdapter;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class GsonMapper implements JsonMapper {
    private  static final Gson gson = new GsonBuilder()
            .serializeNulls()
            .serializeNulls()
            .disableHtmlEscaping()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .registerTypeAdapter(java.sql.Date.class, new SqlDateDeserializer())
            .registerTypeAdapter(LocalDateTime.class, new CustomLocalDateTimeDeserializer())
            .registerTypeAdapter(Date.class, new CustomDateDeserializer())
            .registerTypeAdapter(Timestamp.class, new CustomTimestampDeserializer())
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .registerTypeAdapter(LocalTime.class, new LocalTimeTypeAdapter())
            .setPrettyPrinting()
            .registerTypeHierarchyAdapter(byte[].class,
                    new ByteArrayToBase64TypeAdapter()).create();

    public static Gson getGson() {
        return gson;
    }

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

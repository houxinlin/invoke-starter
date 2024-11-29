package com.cool.request.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomTimestampDeserializer implements JsonDeserializer<Timestamp> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Timestamp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.getAsString().isEmpty()) {
            return null;
        }

        String dateStr = json.getAsString();
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateStr, DATE_FORMATTER);
            return Timestamp.valueOf(localDateTime);
        } catch (Exception e) {
            try {
                long timestamp = json.getAsLong();
                return new Timestamp(timestamp);
            } catch (NumberFormatException ex) {
                throw new JsonParseException("Invalid Timestamp format: " + dateStr, ex);
            }
        }
    }
}

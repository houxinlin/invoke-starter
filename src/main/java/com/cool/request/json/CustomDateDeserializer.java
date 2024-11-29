package com.cool.request.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomDateDeserializer implements JsonDeserializer<Date> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.getAsString().isEmpty()) {
            return null;
        }

        String dateStr = json.getAsString();
        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            try {
                return new Date(json.getAsLong());
            } catch (NumberFormatException ex) {
                throw new JsonParseException("Invalid date format: " + dateStr, ex);
            }
        }
    }
}

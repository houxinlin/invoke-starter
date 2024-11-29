package com.cool.request.json;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class SqlDateDeserializer implements JsonDeserializer<java.sql.Date> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public java.sql.Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || json.getAsString().isEmpty()) {
            return null;
        }

        String dateStr = json.getAsString();
        try {
            java.util.Date parsedDate = DATE_FORMAT.parse(dateStr);
            return new java.sql.Date(parsedDate.getTime());
        } catch (ParseException e) {
            try {
                return new java.sql.Date(json.getAsLong());
            } catch (NumberFormatException ex) {
                throw new JsonParseException("Invalid java.sql.Date format: " + dateStr, ex);
            }
        }
    }
}

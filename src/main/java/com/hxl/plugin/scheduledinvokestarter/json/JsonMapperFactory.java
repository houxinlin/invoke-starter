package com.hxl.plugin.scheduledinvokestarter.json;

public class JsonMapperFactory {
    public static JsonMapper getJsonMapper(String json) {
        if (null == json) return getJsonMapper();
        try {
            Class.forName(json);
            if ("com.google.gson.Gson".equals(json)) return new GsonMapper();
            if ("com.fasterxml.jackson.databind.ObjectMapper".equals(json)) return new ObjectMapperJsonMapper();
            if ("com.alibaba.fastjson2.JSON".equals(json)) return new AlibabaFastJson2Mapper();
            if ("com.alibaba.fastjson.JSON".equals(json)) return new AlibabaFastJsonMapper();
        } catch (ClassNotFoundException ignored) {

        }
        return getJsonMapper();
    }

    public static JsonMapper getJsonMapper() {
        try {
            Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            return new ObjectMapperJsonMapper();
        } catch (ClassNotFoundException ignored) {

        }

        try {
            Class.forName("com.google.gson.Gson");
            return new GsonMapper();
        } catch (ClassNotFoundException ignored) {

        }

        try {
            Class.forName("com.alibaba.fastjson2.JSON");
            return new AlibabaFastJson2Mapper();
        } catch (ClassNotFoundException ignored) {

        }
        try {
            Class.forName("com.alibaba.fastjson.JSON");
            return new AlibabaFastJsonMapper();
        } catch (ClassNotFoundException ignored) {

        }

        return null;
    }
}

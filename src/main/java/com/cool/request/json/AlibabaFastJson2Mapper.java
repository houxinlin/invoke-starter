package com.cool.request.json;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import java.util.Map;

public class AlibabaFastJson2Mapper implements JsonMapper{
    @Override
    public Map<String, Object> toMap(String json) {
        return JSON.parseObject(json, new TypeReference<Map<String, Object> >() {});
    }
    @Override
    public <T> T toBean(String json, Class<T> tClass) throws JsonException {
        return JSON.parseObject(json,tClass);
    }

    @Override
    public String toJSONString(Object o) {
        return JSON.toJSONString(o);
    }
}

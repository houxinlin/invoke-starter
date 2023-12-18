package com.hxl.plugin.scheduledinvokestarter.json;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.Map;

public class AlibabaFastJsonMapper  implements JsonMapper{
    @Override
    public Map<String, Object> toMap(String json) {
        return JSON.parseObject(json, new TypeReference<Map<String, Object> >() {});
    }

    @Override
    public String toJSONString(Object o) {
        return com.alibaba.fastjson.JSON.toJSONString(o);
    }
}

package com.hxl.plugin.scheduledinvokestarter;

import com.hxl.plugin.scheduledinvokestarter.model.InvokeResponseModel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;

public class ExceptionInvokeResponseModel extends InvokeResponseModel {
    public ExceptionInvokeResponseModel(String id, Exception e) {
        setData(Optional.ofNullable(e.getMessage()).orElse("").getBytes(StandardCharsets.UTF_8));
        setHeader(new ArrayList<>());
        setId(id);
        setType("");
    }
}

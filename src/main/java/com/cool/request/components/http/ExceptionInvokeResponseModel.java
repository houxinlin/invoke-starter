package com.cool.request.components.http;

import com.cool.request.components.http.response.InvokeResponseModel;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Optional;

public class ExceptionInvokeResponseModel extends InvokeResponseModel implements Serializable {
    private static final long serialVersionUID = 1000000;

    public ExceptionInvokeResponseModel(String id, Throwable e) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        e.printStackTrace(new PrintWriter(byteArrayOutputStream));
        setBaseBodyData(Base64.getEncoder().encodeToString(byteArrayOutputStream.toString().getBytes(StandardCharsets.UTF_8)));
        setHeader(new ArrayList<>());
        setId(id);
    }
}

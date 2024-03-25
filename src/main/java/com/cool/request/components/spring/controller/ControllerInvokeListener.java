package com.cool.request.components.spring.controller;

import com.cool.request.components.ComponentListener;
import com.cool.request.components.http.ReflexHttpRequestParamAdapterBody;
import com.cool.request.components.http.response.InvokeResponseModel;

public interface ControllerInvokeListener extends ComponentListener {
    public InvokeResponseModel invokeController(ReflexHttpRequestParamAdapterBody reflexHttpRequestParamAdapterBody);
}

package com.cool.request.rmi.starter;

import com.cool.request.components.ComponentListener;
import com.cool.request.components.ComponentLoader;
import com.cool.request.components.http.ReflexHttpRequestParamAdapterBody;
import com.cool.request.components.http.response.InvokeResponseModel;
import com.cool.request.components.method.MethodComponentListener;
import com.cool.request.components.method.RMICallMethod;
import com.cool.request.components.scheduled.ScheduledListener;
import com.cool.request.components.spring.controller.ControllerInvokeListener;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CoolRequestStarterRMIImpl extends UnicastRemoteObject implements ICoolRequestStarterRMI {
    private final ComponentLoader componentLoader;

    public CoolRequestStarterRMIImpl(ComponentLoader componentLoader) throws RemoteException {
        this.componentLoader = componentLoader;
    }

    @Override
    public String invokeMethod(RMICallMethod rmiCallMethod) throws RemoteException {
        for (ComponentListener componentListener : componentLoader.getComponentListeners()) {
            if (componentListener instanceof MethodComponentListener) {
                return ((MethodComponentListener) componentListener)
                        .invokeMethod(rmiCallMethod);
            }
        }
        return null;
    }

    @Override
    public InvokeResponseModel invokeController(
            ReflexHttpRequestParamAdapterBody reflexHttpRequestParamAdapterBody)
            throws RemoteException {
        for (ComponentListener componentListener : componentLoader.getComponentListeners()) {
            if (componentListener instanceof ControllerInvokeListener) {
                return ((ControllerInvokeListener) componentListener)
                        .invokeController(reflexHttpRequestParamAdapterBody);
            }
        }
        return null;
    }

    @Override
    public boolean invokeScheduled(String className, String methodName, String param) throws RemoteException {
        for (ComponentListener componentListener : componentLoader.getComponentListeners()) {
            if (componentListener instanceof ScheduledListener) {
                ((ScheduledListener) componentListener)
                        .invokeScheduled(className, methodName, param);
            }
        }
        return false;
    }
}

package com.cool.request.components.method;

import com.cool.request.components.ComponentListener;

import java.rmi.RemoteException;

public interface MethodComponentListener  extends ComponentListener {
    public String invokeMethod(RMICallMethod rmiCallMethod) throws RemoteException;
}

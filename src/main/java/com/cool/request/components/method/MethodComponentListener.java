package com.cool.request.components.method;

import com.cool.request.components.ComponentListener;
import com.cool.request.rmi.starter.CallResult;

import java.rmi.RemoteException;
import java.util.List;

public interface MethodComponentListener extends ComponentListener {
    public List<Integer> getHasCode(RMICallMethod rmiCallMethod) throws RemoteException;

    public CallResult invokeMethod(RMICallMethod rmiCallMethod, int hasCode) throws RemoteException;
}

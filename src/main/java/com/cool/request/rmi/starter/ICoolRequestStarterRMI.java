package com.cool.request.rmi.starter;

import com.cool.request.components.http.ReflexHttpRequestParamAdapterBody;
import com.cool.request.components.http.response.InvokeResponseModel;
import com.cool.request.components.method.RMICallMethod;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ICoolRequestStarterRMI extends Remote {
    public List<Integer> getHasCode(RMICallMethod rmiCallMethod) throws RemoteException;

    public boolean ping() throws RemoteException;

    public CallResult invokeMethod(RMICallMethod rmiCallMethod, int hasCode, byte[] code) throws RemoteException;

    public InvokeResponseModel invokeController(
            ReflexHttpRequestParamAdapterBody reflexHttpRequestParamAdapterBody)
            throws RemoteException;

    public boolean invokeScheduled(String className, String methodName, String param) throws RemoteException;
    public String getSpringBootStartupMainClass() throws RemoteException;
    public void invokeTestMethod(String className, String methodName, String classRoot, String... methodClassName) throws RemoteException;
}

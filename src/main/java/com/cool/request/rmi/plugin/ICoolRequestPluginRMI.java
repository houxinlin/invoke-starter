package com.cool.request.rmi.plugin;

import com.cool.request.components.http.DynamicController;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ICoolRequestPluginRMI extends Remote {
    public void loadController(List<DynamicController> dynamicController) throws RemoteException;

    public void projectStartup(int availableTcpPort, int serverPort) throws RemoteException;

    public void loadGateway(String contextPath, int port, String prefix, String id) throws RemoteException;
}

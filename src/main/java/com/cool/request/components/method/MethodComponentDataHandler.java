package com.cool.request.components.method;

import com.cool.request.components.ComponentDataHandler;
import com.cool.request.components.SpringBootStartInfo;
import org.springframework.context.ApplicationContext;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.function.BiConsumer;

public class MethodComponentDataHandler implements ComponentDataHandler, MethodComponentListener {
    private ApplicationContext applicationContext;
    private SpringBootStartInfo springBootStartInfo;

    public MethodComponentDataHandler(ApplicationContext applicationContext, SpringBootStartInfo springBootStartInfo) {
        this.applicationContext = applicationContext;
        this.springBootStartInfo = springBootStartInfo;
    }

    @Override
    public String invokeMethod(RMICallMethod rmiCallMethod) throws RemoteException {
        try {
            System.out.println(rmiCallMethod.getClassName());
            Class<?> aClass = Class.forName(rmiCallMethod.getClassName(), false, ClassLoader.getSystemClassLoader());
            Map<String, ?> beansOfType = applicationContext.getBeansOfType(aClass);
            beansOfType.forEach(new BiConsumer<String, Object>() {
                @Override
                public void accept(String s, Object object) {
                    System.out.println(s + " " + object);
                }
            });

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    @Override
    public void componentInit(ApplicationContext applicationContext) {

    }
}

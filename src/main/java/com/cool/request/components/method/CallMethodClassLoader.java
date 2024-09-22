package com.cool.request.components.method;

public class CallMethodClassLoader extends ClassLoader {
    public static final String CLASS_NAME_METHOD_CALL = "dev.coolrequest.function.CoolRequestMethodCall";
    private final byte[] classData;

    public CallMethodClassLoader(byte[] classData) {
        super(ClassLoader.getSystemClassLoader());
        this.classData = classData;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.equals(CLASS_NAME_METHOD_CALL)) {
            return defineClass(name, classData, 0, classData.length);
        }
        return super.findClass(name);
    }
}
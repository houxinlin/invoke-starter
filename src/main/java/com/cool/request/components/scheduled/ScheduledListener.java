package com.cool.request.components.scheduled;

import com.cool.request.components.ComponentListener;

public interface ScheduledListener extends ComponentListener {
    public void invokeScheduled(String className, String methodName, String param);

}

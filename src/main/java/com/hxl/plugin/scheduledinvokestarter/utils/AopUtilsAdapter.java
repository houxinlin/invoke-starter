package com.hxl.plugin.scheduledinvokestarter.utils;

import org.springframework.aop.framework.Advised;
import org.springframework.util.Assert;

public class AopUtilsAdapter {
    public static <T> T getTargetObject(Object candidate) {
        Assert.notNull(candidate, "Candidate must not be null");
        try {
            if (org.springframework.aop.support.AopUtils.isAopProxy(candidate) && candidate instanceof Advised) {
                Object target = ((Advised) candidate).getTargetSource().getTarget();
                if (target != null) {
                    return (T) target;
                }
            }
        }
        catch (Throwable ex) {
            throw new IllegalStateException("Failed to unwrap proxied object", ex);
        }
        return (T) candidate;
    }
}

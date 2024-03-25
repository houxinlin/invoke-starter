package com.cool.request.utils;

import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collection;

public class AnnotationUtilsAdapter {
    public static boolean isCandidateClass(Class<?> clazz, Collection<Class<? extends Annotation>> annotationTypes) {
        for (Class<? extends Annotation> annotationType : annotationTypes) {
            if (isCandidateClass(clazz, annotationType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCandidateClass(Class<?> clazz, @Nullable Class<? extends Annotation> annotationType) {
        return (annotationType != null && isCandidateClass(clazz, annotationType.getName()));
    }

    public static boolean isCandidateClass(Class<?> clazz, String annotationName) {
        if (annotationName.startsWith("java.")) {
            return true;
        }
        if (hasPlainJavaAnnotationsOnly(clazz)) {
            return false;
        }
        return true;
    }

    static boolean hasPlainJavaAnnotationsOnly(Class<?> type) {
        return (type.getName().startsWith("java.") || type == Ordered.class);
    }
}

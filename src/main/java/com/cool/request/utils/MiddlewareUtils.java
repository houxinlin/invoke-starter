package com.cool.request.utils;

public class MiddlewareUtils {
    public static boolean hasSpringGateway() {
        try {
            Class.forName("org.springframework.cloud.gateway.route.RouteLocator");
            return true;
        } catch (ClassNotFoundException e) {

        }
        return false;
    }
}

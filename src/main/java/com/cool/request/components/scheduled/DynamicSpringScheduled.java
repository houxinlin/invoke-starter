package com.cool.request.components.scheduled;

import com.cool.request.ScheduledEndpoint;

import java.io.Serializable;

public class DynamicSpringScheduled extends SpringScheduled implements Serializable {
    private static final long serialVersionUID = 1000000000;

    private transient ScheduledEndpoint attachScheduledEndpoint;

    public ScheduledEndpoint getAttachScheduledEndpoint() {
        return attachScheduledEndpoint;
    }

    public void setAttachScheduledEndpoint(ScheduledEndpoint attachScheduledEndpoint) {
        this.attachScheduledEndpoint = attachScheduledEndpoint;
    }

    public static final class DynamicSpringScheduledBuilder {
        private String moduleName;
        private int serverPort;
        private String className;
        private String methodName;
        private String springInnerId;

        private DynamicSpringScheduledBuilder() {
        }

        public static DynamicSpringScheduledBuilder aDynamicSpringScheduled() {
            return new DynamicSpringScheduledBuilder();
        }

        public DynamicSpringScheduledBuilder withModuleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }

        public DynamicSpringScheduledBuilder withServerPort(int serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public DynamicSpringScheduledBuilder withClassName(String className) {
            this.className = className;
            return this;
        }

        public DynamicSpringScheduledBuilder withMethodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public DynamicSpringScheduledBuilder withSpringInnerId(String springInnerId) {
            this.springInnerId = springInnerId;
            return this;
        }

        public DynamicSpringScheduled build() {
            DynamicSpringScheduled dynamicSpringScheduled = new DynamicSpringScheduled();
            dynamicSpringScheduled.setModuleName(moduleName);
            dynamicSpringScheduled.setServerPort(serverPort);
            dynamicSpringScheduled.setClassName(className);
            dynamicSpringScheduled.setMethodName(methodName);
            return dynamicSpringScheduled;
        }
    }
}

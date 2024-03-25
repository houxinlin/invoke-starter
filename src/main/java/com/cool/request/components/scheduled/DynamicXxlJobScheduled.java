package com.cool.request.components.scheduled;

import com.cool.request.ScheduledEndpoint;

import java.io.Serializable;

public class DynamicXxlJobScheduled extends XxlJobScheduled implements Serializable {
    private static final long serialVersionUID = 1000000000;

    private transient ScheduledEndpoint attachScheduledEndpoint;

    public ScheduledEndpoint getAttachScheduledEndpoint() {
        return attachScheduledEndpoint;
    }

    public void setAttachScheduledEndpoint(ScheduledEndpoint attachScheduledEndpoint) {
        this.attachScheduledEndpoint = attachScheduledEndpoint;
    }

    public static final class DynamicXxlJobScheduledBuilder {
        private String moduleName;
        private int serverPort;
        private String className;
        private String methodName;
        private String springInnerId;

        private DynamicXxlJobScheduledBuilder() {
        }

        public static DynamicXxlJobScheduledBuilder aDynamicXxlJobScheduled() {
            return new DynamicXxlJobScheduledBuilder();
        }

        public DynamicXxlJobScheduledBuilder withModuleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }

        public DynamicXxlJobScheduledBuilder withServerPort(int serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public DynamicXxlJobScheduledBuilder withClassName(String className) {
            this.className = className;
            return this;
        }

        public DynamicXxlJobScheduledBuilder withMethodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public DynamicXxlJobScheduledBuilder withSpringInnerId(String springInnerId) {
            this.springInnerId = springInnerId;
            return this;
        }

        public DynamicXxlJobScheduled build() {
            DynamicXxlJobScheduled dynamicXxlJobScheduled = new DynamicXxlJobScheduled();
            dynamicXxlJobScheduled.setModuleName(moduleName);
            dynamicXxlJobScheduled.setServerPort(serverPort);
            dynamicXxlJobScheduled.setClassName(className);
            dynamicXxlJobScheduled.setMethodName(methodName);
            return dynamicXxlJobScheduled;
        }
    }
}

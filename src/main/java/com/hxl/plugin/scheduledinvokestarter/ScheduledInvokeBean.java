package com.hxl.plugin.scheduledinvokestarter;

public class ScheduledInvokeBean  extends InvokeBean{
    private String className;
    private String methodName;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public static final class ScheduledInvokeBeanBuilder {
        private String id;
        private String className;
        private String methodName;

        private ScheduledInvokeBeanBuilder() {
        }

        public static ScheduledInvokeBeanBuilder aScheduledInvokeBean() {
            return new ScheduledInvokeBeanBuilder();
        }

        public ScheduledInvokeBeanBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public ScheduledInvokeBeanBuilder withClassName(String className) {
            this.className = className;
            return this;
        }

        public ScheduledInvokeBeanBuilder withMethodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public ScheduledInvokeBean build() {
            ScheduledInvokeBean scheduledInvokeBean = new ScheduledInvokeBean();
            scheduledInvokeBean.setId(id);
            scheduledInvokeBean.setClassName(className);
            scheduledInvokeBean.setMethodName(methodName);
            return scheduledInvokeBean;
        }
    }
}

package com.hxl.plugin.scheduledinvokestarter.model;

import java.util.List;

public class InvokeResponseModel extends Model {
    public static class Header {
        private String key;
        private String value;

        public Header() {
        }

        public Header(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private List<Header> header;
    private byte[] data;
    private String id;

    public String headerToString() {
        StringBuilder headerStringBuffer = new StringBuilder();
        for (InvokeResponseModel.Header header : getHeader()) {
            headerStringBuffer.append(header.getKey()).append(": ").append(header.getValue());
            headerStringBuffer.append("\n");
        }
        return headerStringBuffer.toString();
    }

    public List<Header> getHeader() {
        return header;
    }

    public void setHeader(List<Header> header) {
        this.header = header;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static final class InvokeResponseModelBuilder {
        private List<Header> header;
        private byte[] data;
        private String id;
        private String type;

        private InvokeResponseModelBuilder() {
        }

        public static InvokeResponseModelBuilder anInvokeResponseModel() {
            return new InvokeResponseModelBuilder();
        }

        public InvokeResponseModelBuilder withHeader(List<Header> header) {
            this.header = header;
            return this;
        }

        public InvokeResponseModelBuilder withData(byte[] data) {
            this.data = data;
            return this;
        }

        public InvokeResponseModelBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public InvokeResponseModelBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public InvokeResponseModel build() {
            InvokeResponseModel invokeResponseModel = new InvokeResponseModel();
            invokeResponseModel.setHeader(header);
            invokeResponseModel.setData(data);
            invokeResponseModel.setId(id);
            invokeResponseModel.setType(type);
            return invokeResponseModel;
        }
    }
}

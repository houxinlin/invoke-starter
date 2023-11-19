package com.hxl.plugin.scheduledinvokestarter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    static class Test{
        private byte[] a ;

        public byte[] getA() {
            return a;
        }

        public void setA(byte[] a) {
            this.a = a;
        }
    }
    public static void main(String[] args) {
        Test test = new Test();
        test.setA("asd".getBytes());
        try {
            System.out.println(new ObjectMapper().writeValueAsString(test));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.hxl.plugin.scheduledinvokestarter;

import jdk.nashorn.internal.runtime.linker.Bootstrap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

interface Woo { public void wow(); }
interface Moo { public String mow(); }
interface Boo { public int bow(); }
public class Main {
    static void doIt(Woo w) {
        System.out.print("W");
        w.wow();
    }
  static   class Car implements Serializable {
        class Engine implements Serializable { }
        private Engine e;
        public void Car() {
            e = new Engine();
        }
        public Engine getEngine() { return e; }
    }
    static String doIt(Moo m) {
        System.out.print("M");
        return m.mow();
    }
    static int doIt(Boo b) {
        System.out.print("B");
        return b.bow();
    }
    public static void main(String[] args) {

    }
}

package com.hxl.plugin.scheduledinvokestarter.utils;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketUtils {
    public static int findAvailableTcpPort() {
      return  findAvailableTcpPort(6666);
    }
    public static int findAvailableTcpPort(int startPort) {
        while (startPort <= 65535) {
            if (isPortAvailable(startPort)) {
                return startPort;
            } else {
                startPort++;
            }
        }
        throw new IllegalStateException("No available TCP port found");
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

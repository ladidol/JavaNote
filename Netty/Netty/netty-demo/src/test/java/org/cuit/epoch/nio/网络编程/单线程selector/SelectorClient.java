package org.cuit.epoch.nio.网络编程.单线程selector;

import java.io.IOException;
import java.net.Socket;

public class SelectorClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8088)) {
            System.out.println(socket);
            socket.getOutputStream().write("ladidol".getBytes());
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
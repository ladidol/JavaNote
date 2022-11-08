package org.cuit.epoch.nio.网络编程.多线程selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * @author: ladidol
 * @date: 2022/11/8 15:33
 * @description:
 */
public class TestClient {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));
        sc.write(Charset.defaultCharset().encode("hello MultiplyThread！"));
        System.in.read();

    }
}
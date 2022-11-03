package org.cuit.epoch.网络编程.单线程;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * @author: ladidol
 * @date: 2022/11/1 16:17
 * @description:
 */
public class Client {

    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8088));
        sc.write(Charset.defaultCharset().encode("hello! ladidol!"));
        System.out.println("waiting...");
        Scanner scan = new Scanner(System.in);
        int n = scan.nextInt();
    }

}
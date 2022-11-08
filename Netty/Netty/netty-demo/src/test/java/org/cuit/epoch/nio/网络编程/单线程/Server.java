package org.cuit.epoch.nio.网络编程.单线程;

import lombok.extern.slf4j.Slf4j;
import org.cuit.epoch.nio.bytebuffer.ByteBufferUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: ladidol
 * @date: 2022/11/1 16:03
 * @description:
 */
@Slf4j
public class Server {


    public static void main(String[] args) throws IOException {

        m2();

    }

    /**
     * 参数：[]
     * 返回值：void
     * 作者： ladidol
     * 描述：单线程非阻塞模式
     */
    private static void m2() throws IOException {
        // 使用 nio 来理解非阻塞模式, 单线程
        // 0. ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);
        // 1. 创建了服务器，设置为非阻塞模式
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); // 非阻塞模式
        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8088));
        // 3. 连接集合
        List<SocketChannel> channels = new ArrayList<>();
        while (true) {
            // 4. accept 建立与客户端连接， SocketChannel 用来与客户端之间通信
            SocketChannel sc = ssc.accept(); // 非阻塞，线程还会继续运行，如果没有连接建立，但sc是null
            if (sc != null) {
                log.debug("connected... {}", sc);
                sc.configureBlocking(false); // 非阻塞模式
                channels.add(sc);
            }
            for (SocketChannel channel : channels) {
                // 5. 接收客户端发送的数据
                int read = channel.read(buffer);// 非阻塞，线程仍然会继续运行，如果没有读到数据，read 返回 0
                if (read > 0) {
                    buffer.flip();
                    ByteBufferUtil.debugRead(buffer);
                    buffer.clear();
                    log.debug("after read...{}", channel);
                }
            }
        }
    }

    /**
     * 参数：[]
     * 返回值：void
     * 作者： ladidol
     * 描述：单线程的阻塞模式
     */
    private static void m1() throws IOException {
        // 使用nio 来理解阻塞模式,这里特地用的单线程来实现。
        // 0. ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);


        // 1. 创建服务器。
        ServerSocketChannel ssc = ServerSocketChannel.open();


        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8088));


        // 3. 连接集合
        List<SocketChannel> channels = new ArrayList<>();

        while (true) {
            // 4. accept 建立于客户端建立连接，SocketChannel 用来于客户端通信
            log.debug("建立新链接ing");
            SocketChannel sc = ssc.accept();
            channels.add(sc);
            log.debug("已经建立新链接 {}", sc);

            // 5. 接收全部客户端发送的数据
            for (SocketChannel channel : channels) {

                log.debug("before read ...{}", sc);
                channel.read(buffer);
                buffer.flip();
                ByteBufferUtil.debugRead(buffer);
                buffer.clear();
                log.debug("after read ...{}", sc);
            }
        }
    }


}
package org.cuit.epoch.nio.网络编程.单线程selector;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.cuit.epoch.nio.bytebuffer.ByteBufferUtil.debugRead;

/**
 * @author: ladidol
 * @date: 2022/11/3 15:01
 * @description:
 */
@Slf4j
public class SelectorServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.bind(new InetSocketAddress(8088));
        System.out.println(channel);
        Selector selector = Selector.open();
        channel.configureBlocking(false);
        SelectionKey ssckey = channel.register(selector, SelectionKey.OP_ACCEPT);//对服务器Channel使用accept事件监听。
        log.debug("register key:{}", ssckey);

        while (true) {
            int count = selector.select();
//                int count = selector.selectNow();
            log.debug("select count: {}", count);//收到一个事件
//                if(count <= 0) {
//                    continue;
//                }

            // 获取所有事件
            Set<SelectionKey> keys = selector.selectedKeys();

            // 遍历所有事件，逐一处理
            Iterator<SelectionKey> iter = keys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                log.debug("当前事件key: {}", key);
                log.debug("keys.size() = " + keys.size());

                // 判断事件类型
                if (key.isAcceptable()) {
                    ServerSocketChannel c = (ServerSocketChannel) key.channel();
                    // 必须处理
                    SocketChannel sc = c.accept();
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);//对收到的普通socketChannel监听read事件
                    log.debug("连接已建立: {}", sc);
                } else if (key.isReadable()) {
                    try {
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(128);
                        int read = sc.read(buffer);
                        if (read == -1) {
                            key.cancel();//这里表示是客户端通过sc.close()，正常断开，read值是-1，因此可以使用 key 取消。
                            sc.close();
                        } else {
                            buffer.flip();
                            debugRead(buffer);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        key.cancel();//因为客户端异常断开了（就是直接强制红stop），因此需要将 key 取消。
                    }
                }
                // 处理完毕，必须将事件从selectedKeys中移除
                iter.remove();
            }
        }

    }


    /**
     * 参数：[]
     * 返回值：void
     * 作者： ladidol
     * 描述：处理accept事件
     */
    private static void m1() throws IOException {
        // 1. 建立selector，管理多规格channel
        Selector selector = Selector.open();


        ByteBuffer buffer = ByteBuffer.allocate(16);
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); // 非阻塞模式

        //2. 建立selector和channel的联系（注册）
        //SelectionKey 就是将来时间发生后，通过他可以知道时间和那个channel的事件。
        SelectionKey ssckey = ssc.register(selector, 0, null);
        // key只关注accept事件
        ssckey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("register key:{}", ssckey);

        ssc.bind(new InetSocketAddress(8088));
        List<SocketChannel> channels = new ArrayList<>();
        while (true) {
            //3. select 方法,没有事件发生，线程阻塞；有事件，线程才会恢复运行
            selector.select();

            //4. 处理事件
            //用迭代器可以边遍历边删除set中的元素。
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iter = keys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                log.debug("key: {}", key);
                SelectableChannel channelInKey = key.channel();
                ServerSocketChannel channel = (ServerSocketChannel) channelInKey;
                SocketChannel sc = channel.accept();
                log.debug("{}", sc);
                log.debug("{}", keys.size());
            }
        }
    }


}
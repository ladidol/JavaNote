package org.cuit.epoch.netty.eventloop;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Date;

/**
 * @author: ladidol
 * @date: 2022/11/8 16:56
 * @description:
 */
public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        // 1. 启动类
        Channel channel = new Bootstrap()
                // 2. 添加 EventLoop 使用选择器和多线程
                .group(new NioEventLoopGroup())
                // 3. 选择客户端的channel实现类：NioSocketChannel.class
                .channel(NioSocketChannel.class)
                // 4. 添加处理器
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new StringEncoder());//，编码器：依旧是字符编码成ByteBuf
                    }
                })
                // 5. 连接服务器
                .connect("127.0.0.1", 8080)
                .sync()
                .channel();

        System.out.println("channel = " + channel);
        System.out.println("");//debug模式中，debug All：停下所有线程；debug Thread：停下当前线程。


//                .writeAndFlush(new Date() + ": hello world!");
    }




    //new Bootstrap()
    //    .group(new NioEventLoopGroup()) // 1
    //    .channel(NioSocketChannel.class) // 2
    //    .handler(new ChannelInitializer<Channel>() { // 3
    //        @Override
    //        protected void initChannel(Channel ch) {
    //            ch.pipeline().addLast(new StringEncoder()); // 8
    //        }
    //    })
    //    .connect("127.0.0.1", 8080) // 4
    //    .sync() // 5
    //    .channel() // 6
    //    .writeAndFlush(new Date() + ": hello world!"); // 7


}
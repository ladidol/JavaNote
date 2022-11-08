package org.cuit.epoch.netty.hello;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

/**
 * @author: ladidol
 * @date: 2022/11/8 16:55
 * @description:
 */
public class HelloServer {
    public static void main(String[] args) {
        // 大概解释一下这段代码：

        // 1. 服务器端的启动器：负责组装netty组件，协调他们工作，启动服务器。
        new ServerBootstrap()
                // 2. BossEventLoop, WorkerEventLoop(selector,thread)，通过group组里面包含了线程和选择器。
                .group(new NioEventLoopGroup())
                // 3. 选择 服务器的ServerSocketChannel 实现,这里的实现是 NioServerSocketChannel.class
                .channel(NioServerSocketChannel.class)
                // 4. boss 负责处理连接 worker(child) 负责处理读写，决定worker（child） 能执行哪些操作（handler）
                .childHandler(
                        // 5. channel 代表和哭护短进行数据读写的通道 Initializer 初始化，负责添加别的 handler。
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) {
                                //6. 添加具体的handler
                                ch.pipeline().addLast(new StringDecoder()); // 将ByteBuf 转换成字符串。
                                ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {// 自定义handler
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                        System.out.println(msg);
                                    }
                                });
                            }
                        })
                .bind(8080);
    }




    //new ServerBootstrap()
    //    .group(new NioEventLoopGroup()) // 1
    //    .channel(NioServerSocketChannel.class) // 2
    //    .childHandler(new ChannelInitializer<NioSocketChannel>() { // 3
    //        protected void initChannel(NioSocketChannel ch) {
    //            ch.pipeline().addLast(new StringDecoder()); // 5
    //            ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() { // 6
    //                @Override
    //                protected void channelRead0(ChannelHandlerContext ctx, String msg) {
    //                    System.out.println(msg);
    //                }
    //            });
    //        }
    //    })
    //    .bind(8080); // 4


}
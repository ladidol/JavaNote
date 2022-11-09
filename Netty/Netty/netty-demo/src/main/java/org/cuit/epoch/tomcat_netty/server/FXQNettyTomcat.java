package org.cuit.epoch.tomcat_netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import lombok.extern.slf4j.Slf4j;
import org.cuit.epoch.tomcat_netty.http.FXQRequest;
import org.cuit.epoch.tomcat_netty.http.FXQResponse;
import org.cuit.epoch.tomcat_netty.http.FXQServlet;

import java.io.FileInputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Tom.
 */
@Slf4j
public class FXQNettyTomcat {
    private int port = 8080;
    private ServerSocket server;
    private Map<String, FXQServlet> servletMapping = new HashMap<String, FXQServlet>();
    private Properties webxml = new Properties();

    private void init() {
        //加载web.xml文件，同时初始化ServletMapping对象
        try {
            String WEB_INF = this.getClass().getResource("/").getPath();
//            log.info("WEB_INF路径为 {}", WEB_INF);
//            WEB_INF = "E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\netty-demo\\src\\main\\resources\\";
            log.info("WEB_INF路径为 {}", WEB_INF);

            FileInputStream fis = new FileInputStream(WEB_INF + "web_netty.properties");
            webxml.load(fis);
            for (Object k : webxml.keySet()) {
                String key = k.toString();
                if (key.endsWith(".url")) {
                    String servletName = key.replaceAll("\\.url$", "");
                    String url = webxml.getProperty(key);
                    String className = webxml.getProperty(servletName + ".className");
                    //单实例，多线程
                    FXQServlet obj = (FXQServlet) Class.forName(className).newInstance();
                    servletMapping.put(url, obj);
                    log.info("url: {} ; Servlet: {}", url, obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        init();
        //Netty封装了NIO的Reactor模型，Boss, Worker
        //Boss线程
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //Worker线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //1.创建对象
            ServerBootstrap server = new ServerBootstrap();
            //2.配置参数
            //链路式编程
            server.group(bossGroup, workerGroup)
                    //主线程处理类，看到这样的写法，底层就是用反射
                    .channel(NioServerSocketChannel.class)
                    //子线程处理类，Handler
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //客户端初始化处理
                        protected void initChannel(SocketChannel client) throws Exception {
                            //无锁化串行编程
                            //Netty对HTTP的封装，对顺序有要求
                            //HttpResponseEncoder编码器
                            //责任链模式，双向链表Inbound OutBound
                            client.pipeline().addLast(new HttpResponseEncoder());
                            //HttpRequestDecoder解码器
                            client.pipeline().addLast(new HttpRequestDecoder());
                            //业务逻辑处理
                            client.pipeline().addLast(new GPTomcatHandler());
                        }
                    })
                    //针对主线程的配置分配线程最大数量128
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //针对子线程的配置保持长连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            //3.启动服务器
            ChannelFuture f = server.bind(port).sync();
            System.out.println("XiaoxiaoTomcat已启动，监听的端口是:" + port);
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭线程池
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    public class GPTomcatHandler extends ChannelInboundHandlerAdapter {


        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpRequest) {
                HttpRequest req = (HttpRequest) msg;
                //转交给我们自己的Request实现
                FXQRequest request = new FXQRequest(ctx, req);
                //转交给我们自己的Response实现
                FXQResponse response = new FXQResponse(ctx, req);
                //实际业务处理
                String url = request.getUrl();
                log.info("hel1o, this time's url is {}", url);
                if (servletMapping.containsKey(url)) {
                    servletMapping.get(url).service(request, response);
                } else {
                    response.write("404 - Not Found");
                }

            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
        }

    }


    public static void main(String[] args) {
        new FXQNettyTomcat().start();
    }


}

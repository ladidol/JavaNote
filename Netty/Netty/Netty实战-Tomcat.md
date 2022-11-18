## 环境准备

Tomcat就是一个web容器，用底层通信框架Netty来实现不难。我们知道，Tomcat是基于J2EE规范的Web容 器，主要入口是web.xml文件。web.xml文件中主要配置Servlet、 Filter、Listener等，而Servlet、Filter、Listener在J2EE中只是抽 象的实现，具体业务逻辑由开发者来实现。本章内容，就以最常用的 Servlet为例来详细展开。

### 1）简易FXQServlet抽象类

```java
package org.cuit.epoch.tomcat.http;

public abstract class FXQServlet {
    public void service(FXQRequest request, FXQResponse response) throws Exception {

        //由service()方法决定是调用doGet()还是调用doPost()
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            doGet(request, response);
        } else {
            doPost(request, response);
        }

    }

    public abstract void doGet(FXQRequest request, FXQResponse response) throws Exception;

    public abstract void doPost(FXQRequest request, FXQResponse response) throws Exception;

}
```

doGet()方法和doPost()方法中有两 个参数FXQRequest和FXQResponse对象，是由web容器创建的，主要是对底层Socket的输入输出的封装。其中FXQRequest是对 Input的封装，FXQResponse是对Output的封装。

### 2）创建用户业务代码

继承FXQServlet抽象类的两个简单Servlet：

```java
package org.cuit.epoch.tomcat.servlet;

import org.cuit.epoch.tomcat.http.FXQRequest;
import org.cuit.epoch.tomcat.http.FXQResponse;
import org.cuit.epoch.tomcat.http.FXQServlet;

public class FirstServlet extends FXQServlet {
    public void doGet(FXQRequest request, FXQResponse response) throws Exception {
        this.doPost(request, response);
    }
    public void doPost(FXQRequest request, FXQResponse response) throws Exception {
        response.write("This is First Servlet");
    }
}

```



```java
package org.cuit.epoch.tomcat.servlet;

import org.cuit.epoch.tomcat.http.FXQRequest;
import org.cuit.epoch.tomcat.http.FXQResponse;
import org.cuit.epoch.tomcat.http.FXQServlet;

public class SecondServlet extends FXQServlet {
    public void doGet(FXQRequest request, FXQResponse response) throws Exception {
        this.doPost(request, response);

    }

    public void doPost(FXQRequest request, FXQResponse response) throws Exception {
        response.write("This is Second Servlet");
    }
}
```



### 3）web.properties配置文件

用web.properties文件代替web.xml文件，具 体内容如下。

```properties
servlet.one.url=/firstServlet.do
servlet.one.className=org.cuit.epoch.tomcat.servlet.FirstServlet
servlet.two.url=/secondServlet.do
servlet.two.className=org.cuit.epoch.tomcat.servlet.SecondServlet
```

## 传统IO实现Tomcat

### 1）创建FXQRequest对象

FXQRequest主要就是对HTTP的请求头信息进行解析。我们从浏览器 发 送 一 个 HTTP 请 求 ， 如 在 浏 览 器 地 址 栏 中 输 入 http://localhost:8080，后台服务器获取的请求其实就是一串字符 串，具体格式如下。

![image-20221109171220685](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202211091712895.png)



在GPRequest获得输入内容之后，对这一串满足HTTP的字符信息进 行解析。我们来看GPRequest简单直接的代码实现。

FXQRequest

```java
package org.cuit.epoch.tomcat.http;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * @author: ladidol
 * @date: 2022/11/9 9:31
 * @description:
 */
@Slf4j
public class FXQRequest {
    private String method;
    private String url;

    public FXQRequest(InputStream in) {
        try {
//获取HTTP内容
            String content = "";
            byte[] buff = new byte[1024];
            int len = 0;
            if ((len = in.read(buff)) > 0) {
                content = new String(buff, 0, len);
            }
            log.info("content: \n{}", content);
            String line = content.split("\\n")[0];
            String[] arr = line.split("\\s");
            this.method = arr[0];
            this.url = arr[1].split("\\?")[0];

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }
}
```

主 要 提 供 了 getUrl() 方 法 和 getMethod()方法。输入流InputStream作为GPRequest的构造参数传 入，在构造函数中，用字符串切割的方法提取请求方式和URL。



### 2）创建FXQResponse对象

与GPRequest的实现思路类似，就是 按照HTTP规范从Output输出格式化的字符串，

FXQResponse

```java
package org.cuit.epoch.tomcat.http;

import java.io.OutputStream;

/**
 * @author: ladidol
 * @date: 2022/11/9 9:31
 * @description:
 */
public class FXQResponse {
    private OutputStream out;

    public FXQResponse(OutputStream out) {
        this.out = out;
    }

    public void write(String s) throws Exception {
//输出也要遵循HTTP
//状态码为200
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\n")
                .append("Content-Type: text/html;\n")
                .append("\r\n")
                .append(s);
        out.write(sb.toString().getBytes());
    }
}
```

主要提供了一个write()方法。通过write()方法按照HTTP规范 输出字符串。

### 3）创建FXQTomcat启动类

前两个小节只是对J2EE规范的再现，接下来就是真正 Web容器的实现逻辑，分为三个阶段：初始化阶段、服务就绪阶段、接 受请求阶段。

第一阶段：初始化阶段，主要是完成对web.xml文件的解析。

第二阶段：服务就绪阶段，完成ServerSocket的准备工作。在 GPTomcat类中增加start()方法。

第三阶段：接受请求阶段，完成每一次请求的处理。在GPTomcat 中增加process()方法的实现。

总流程：每次客户端请求过来以后，从servletMapping中获取其对应的 Servlet对象，同时实例化GPRequest和GPResponse对象，将GPRequest 和GPResponse对象作为参数传入service()方法，最终执行业务逻辑。 最后，增加main()方法。

```java
package org.cuit.epoch.tomcat.server;

import lombok.extern.slf4j.Slf4j;
import org.cuit.epoch.tomcat.http.FXQRequest;
import org.cuit.epoch.tomcat.http.FXQResponse;
import org.cuit.epoch.tomcat.http.FXQServlet;


import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Tom.
 */
@Slf4j
public class FXQTomcat {
    private int port = 8080;
    private ServerSocket server;
    private Map<String, FXQServlet> servletMapping = new HashMap<String, FXQServlet>();
    private Properties webxml = new Properties();

    private void init() {
        //加载web.xml文件，同时初始化ServletMapping对象
        try {
            String WEB_INF = this.getClass().getResource("/").getPath();
//            WEB_INF = "E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\netty-demo\\src\\main\\resources\\";
            log.info("WEB_INF路径为 {}", WEB_INF);

            FileInputStream fis = new FileInputStream(WEB_INF + "web.properties");
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
//1.加载配置文件，初始化ServletMapping
        init();
        try {
            server = new ServerSocket(this.port);
            System.out.println("XiaoxiaoTomcat已启动，监听的端口是: " + this.port);
//2.等待用户请求，用一个死循环来等待用户请求
            while (true) {
                Socket client = server.accept();
//3.HTTP请求，发送的数据就是字符串一--有规律的字符串(HTTP)
                process(client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process(Socket client) throws Exception {
        InputStream is = client.getInputStream();
        OutputStream os = client.getOutputStream();
        //4.Request(InputStream)/Response(0utputStream)
        FXQRequest request = new FXQRequest(is);
        FXQResponse response = new FXQResponse(os);
        //5.从协议内容中获得URL,把相应的Servlet用反射进行实例化
        String url = request.getUrl();
        if (servletMapping.containsKey(url)) {
            //6.调用实例化对象的service()方法，执行具体的逻辑doGet()/doPost()方法
            servletMapping.get(url).service(request, response);
        } else {
            response.write("404 - Not Found");
        }
        os.flush();
        os.close();
        is.close();
        client.close();
    }

    public static void main(String[] args) {
        new FXQTomcat().start();
    }
}
```



### 效果：

![image-20221109172220367](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202211091722471.png)

![image-20221109172204419](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202211091722532.png)

## Netty实现Tomcat

代码的基本思路和基于传统I/O手写的版本一致，不再赘述。

### 1）创建FXQRequest对象

```java
package org.cuit.epoch.tomcat_netty.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author: ladidol
 * @date: 2022/11/9 9:31
 * @description:
 */
@Slf4j
public class FXQRequest {

    private ChannelHandlerContext ctx;
    private HttpRequest req;

    public FXQRequest(ChannelHandlerContext ctx, HttpRequest req) {
        this.ctx = ctx;
        this.req = req;
    }

    public String getUrl() {
        return req.uri();
    }

    public String getMethod() {
        return req.method().name();
    }

    public Map<String, List<String>> getParameters() {
        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        return decoder.parameters();
    }

    public String getParameter(String name) {
        Map<String, List<String>> params = getParameters();
        List<String> param = params.get(name);
        if (null == param) {
            return null;
        } else {
            return param.get(0);
        }
    }


}
```

提 供 getUrl() 方 法 和 getMethod()方法。在Netty的版本中，我们增加了getParameter()的 实现，



### 2）创建FXQResponse对象



```java
package org.cuit.epoch.tomcat_netty.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;


/**
 * @author: ladidol
 * @date: 2022/11/9 9:31
 * @description:
 */
public class FXQResponse {

    private ChannelHandlerContext ctx;
    private HttpRequest req;

    public FXQResponse(ChannelHandlerContext ctx, HttpRequest req) {
        this.ctx = ctx;
        this.req = req;
    }

    public void write(String out) throws Exception {
        try {
            if (out == null || out.length() == 0) {
                return;
            }
            //设置HTTP及请求头信息
            FullHttpResponse response = new DefaultFullHttpResponse(
                    //设置版本为HTTP 1.1
                    HttpVersion.HTTP_1_1,
                    //设置响应状态码
                    HttpResponseStatus.OK,
                    //将输出内容编码格式设置为UTF-8
                    Unpooled.wrappedBuffer(out.getBytes("UTF-8")));
            response.headers().set("Content-Type", "text/html;");
            ctx.write(response);
        } finally {
            ctx.flush();
            ctx.close();
        }
    }
}

```

主要提供了一个write()方法。通过write()方法按照HTTP规范 输出字符串。

### 3）创建FXQNettyTomcat启动类



```java
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

```



### 效果：

![image-20221109175323610](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202211091753716.png)

![image-20221109175302228](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202211091753296.png)





## 参考资料

《Netty 4核心原理与手写RPC框架实战》




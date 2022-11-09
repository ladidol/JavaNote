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

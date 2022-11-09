package org.cuit.epoch.tomcat_netty.http;

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












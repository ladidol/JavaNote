package org.cuit.epoch.tomcat_netty.servlet;

import org.cuit.epoch.tomcat_netty.http.FXQRequest;
import org.cuit.epoch.tomcat_netty.http.FXQResponse;
import org.cuit.epoch.tomcat_netty.http.FXQServlet;

public class FirstServlet extends FXQServlet {
    public void doGet(FXQRequest request, FXQResponse response) throws Exception {
        this.doPost(request, response);
    }
    public void doPost(FXQRequest request, FXQResponse response) throws Exception {
        response.write("This is First Servlet");
    }
}

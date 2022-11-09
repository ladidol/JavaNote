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
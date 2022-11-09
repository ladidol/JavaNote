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
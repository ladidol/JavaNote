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
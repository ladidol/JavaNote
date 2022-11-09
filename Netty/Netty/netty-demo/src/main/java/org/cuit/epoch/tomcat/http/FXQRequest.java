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
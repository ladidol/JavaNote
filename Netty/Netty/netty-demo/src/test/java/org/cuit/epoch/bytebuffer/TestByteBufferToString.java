package org.cuit.epoch.bytebuffer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.cuit.epoch.bytebuffer.ByteBufferUtil.debugAll;

/**
 * @author: ladidol
 * @date: 2022/10/29 19:07
 * @description:
 */
public class TestByteBufferToString {

    public static void main(String[] args) {
        // 1. 字符串转为 ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put("hello ladidol".getBytes());
        debugAll(buffer);

        // 2. Charset
        ByteBuffer hello_ladidol = StandardCharsets.UTF_8.encode("hello ladidol");
        debugAll(hello_ladidol);//用这种方法会自动转换成读模式

        // 3. wrap
        ByteBuffer wrap = ByteBuffer.wrap("hello ladidol".getBytes());
        debugAll(wrap);

        // buffer to String
        String s = StandardCharsets.UTF_8.decode(hello_ladidol).toString();
        System.out.println("s = " + s);//注意buffer要切换成读模式。
    }

}
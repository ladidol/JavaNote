package org.cuit.epoch.nio.bytebuffer;

import java.nio.ByteBuffer;

import static org.cuit.epoch.nio.bytebuffer.ByteBufferUtil.debugAll;

/**
 * @author: ladidol
 * @date: 2022/10/29 17:18
 * @description: put & get & compact
 */
public class TestByteBufferReadWrite {

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        debugAll(buffer);
        buffer.put((byte) 0x61);
        debugAll(buffer);

        System.out.println("====================================================================================================================");

        buffer.put(new byte[]{0x62, 0x63, 0x64});
        debugAll(buffer);
        //未切换成读模式
        System.out.println("buffer.get() = " + buffer.get());
        debugAll(buffer);

        //切换成读模式
        buffer.flip();
        System.out.println("buffer.get() = " + buffer.get());// 0x61 = 97.
        debugAll(buffer);

        // compact 切换到写模式
        buffer.compact();
        debugAll(buffer);
        //继续写
        buffer.put(new byte[]{0x65, 0x66, 0x67});
        debugAll(buffer);


    }
}
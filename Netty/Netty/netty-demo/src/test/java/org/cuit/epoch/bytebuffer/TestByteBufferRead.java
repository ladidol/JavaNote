package org.cuit.epoch.bytebuffer;

import java.nio.ByteBuffer;

import static org.cuit.epoch.bytebuffer.ByteBufferUtil.debugAll;

/**
 * @author: ladidol
 * @date: 2022/10/29 18:50
 * @description: rewind & mark & reset & get(i)
 */
public class TestByteBufferRead {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a', 'b', 'c', 'd'});
        buffer.flip();

        // 用 rewind 从头开始读；可以重复读了。
        byte[] bytes = new byte[4];
        buffer.get(bytes);
        debugAll(buffer);
        buffer.rewind();
//        System.out.println("buffer = " + buffer.get());


        // mark & reset
        // mark 做一个标记，记录position 位置，reset 是将 position 重置到 mark 的位置
        debugAll(buffer);
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());
        buffer.mark();//标记索引2的位置。

        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());
        buffer.reset();//重置到mark的位置。

        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());
        buffer.rewind();



        // get(i)
        System.out.println("buffer.get(3) = " + (char) buffer.get(3));
        debugAll(buffer);






    }
}
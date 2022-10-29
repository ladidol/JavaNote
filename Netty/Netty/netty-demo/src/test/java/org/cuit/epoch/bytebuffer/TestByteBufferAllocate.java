package org.cuit.epoch.bytebuffer;

import java.nio.ByteBuffer;

/**
 * @author: ladidol
 * @date: 2022/10/29 18:37
 * @description:
 */
public class TestByteBufferAllocate {

    public static void main(String[] args) {
        System.out.println(ByteBuffer.allocate(16).getClass());// java 堆内存， 读写效率脚底，会收到GC的影响。
        System.out.println(ByteBuffer.allocateDirect(16).getClass());// 直接内存，读写效率高（少一次拷贝），不会受到GC的影响，分配的效率低，如果使用不当会有内存泄漏的危险。
    }
    //class java.nio.HeapByteBuffer
    //class java.nio.DirectByteBuffer

}
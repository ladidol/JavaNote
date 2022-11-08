package org.cuit.epoch.nio.bytebuffer;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author: ladidol
 * @date: 2022/10/29 16:28
 * @description: 用channel和bytebuffer来读取文件。
 */


@Slf4j
public class TestByteBuffer {

    public static void main(String[] args) {
        //FileChannel
        //1. 输入输出流， 2. RandomAccessFile
        //通过try-with-resource能把对文件资源的释放
        try (FileChannel channel = new FileInputStream("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\netty-demo\\data.txt").getChannel()) {
            //准备一个缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);

//            // 从channel中读取数据， 先向 缓冲区buffer 写入
//            channel.read(buffer);
//            // 打印 buffer 的内容
//            buffer.flip();// 切换至读模式
//
//            while (buffer.hasRemaining()){// 是否有剩余的未读数据
//                byte b = buffer.get();//读一个字节
//                System.out.println((char) b);
//            }
//
//            //这里buffer只有10空间，所以一次读取只会从文件中读取10个字节。

            while (true) {
                // 先向 缓冲区buffer 写入
                int len = channel.read(buffer);
                log.debug("读取到的字节数：{}", len);
                if (len == -1) {//没有内容了
                    break;
                }
                // 切换 buffer 至读模式
                buffer.flip();
                while (buffer.hasRemaining()) {// 是否有剩余的未读数据
                    byte b = buffer.get();//读一个字节
                    log.debug("{}", (char) b);
                }
                //切换 buffer 至写模式
                buffer.clear();
            }


        } catch (IOException e) {
            System.out.println("e = " + e);
        }
    }
}
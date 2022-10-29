package org.cuit.epoch.bytebuffer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static org.cuit.epoch.bytebuffer.ByteBufferUtil.debugAll;

/**
 * @author: ladidol
 * @date: 2022/10/29 19:23
 * @description:
 */
public class TestGatheringReads {
    public static void main(String[] args) {
        try (RandomAccessFile file = new RandomAccessFile("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\netty-demo\\3parts.txt", "rw")) {
            FileChannel channel = file.getChannel();
            ByteBuffer d = ByteBuffer.allocate(4);
            ByteBuffer e = ByteBuffer.allocate(4);
            channel.position(11);//跳过字符onetwothree

            d.put(new byte[]{'f', 'o', 'u', 'r'});
            e.put(new byte[]{'f', 'i', 'v', 'e'});
            d.flip();//写入文件之前也要flip一下。
            e.flip();
            debugAll(d);
            debugAll(e);
            channel.write(new ByteBuffer[]{d, e});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //+--------+-------------------- all ------------------------+----------------+
    //position: [0], limit: [4]
    //         +-------------------------------------------------+
    //0         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
    //0+--------+-------------------------------------------------+----------------+
    //0|00000000| 66 6f 75 72                                     |four            |
    //0+--------+-------------------------------------------------+----------------+
    //+--------+-------------------- all ------------------------+----------------+
    //position: [0], limit: [4]
    //         +-------------------------------------------------+
    //0         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
    //0+--------+-------------------------------------------------+----------------+
    //0|00000000| 66 69 76 65                                     |five            |
    //0+--------+-------------------------------------------------+----------------+
}
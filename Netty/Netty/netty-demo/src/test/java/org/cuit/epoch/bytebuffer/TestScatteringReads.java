package org.cuit.epoch.bytebuffer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static org.cuit.epoch.bytebuffer.ByteBufferUtil.debugAll;

/**
 * @author: ladidol
 * @date: 2022/10/29 19:19
 * @description:
 */
public class TestScatteringReads {

    public static void main(String[] args) {
        try (RandomAccessFile file = new RandomAccessFile("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\netty-demo\\3parts.txt", "rw")) {
            FileChannel channel = file.getChannel();
            ByteBuffer a = ByteBuffer.allocate(3);
            ByteBuffer b = ByteBuffer.allocate(3);
            ByteBuffer c = ByteBuffer.allocate(5);
            channel.read(new ByteBuffer[]{a, b, c});
            a.flip();
            b.flip();
            c.flip();
            debugAll(a);
            debugAll(b);
            debugAll(c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //+--------+-------------------- all ------------------------+----------------+
    //position: [0], limit: [3]
    //         +-------------------------------------------------+
    //0         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
    //0+--------+-------------------------------------------------+----------------+
    //0|00000000| 6f 6e 65                                        |one             |
    //0+--------+-------------------------------------------------+----------------+
    //+--------+-------------------- all ------------------------+----------------+
    //position: [0], limit: [3]
    //         +-------------------------------------------------+
    //0         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
    //0+--------+-------------------------------------------------+----------------+
    //0|00000000| 74 77 6f                                        |two             |
    //0+--------+-------------------------------------------------+----------------+
    //+--------+-------------------- all ------------------------+----------------+
    //position: [0], limit: [5]
    //         +-------------------------------------------------+
    //0         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
    //0+--------+-------------------------------------------------+----------------+
    //0|00000000| 74 68 72 65 65                                  |three           |
    //0+--------+-------------------------------------------------+----------------+

}
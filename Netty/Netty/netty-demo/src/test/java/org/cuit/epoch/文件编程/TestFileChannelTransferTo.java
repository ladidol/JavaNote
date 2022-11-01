package org.cuit.epoch.文件编程;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @author: ladidol
 * @date: 2022/11/1 14:19
 * @description: 两个channel的数据传输。
 */
public class TestFileChannelTransferTo {


//    // 小于2g的数据，直接转换就行
//    public static void main(String[] args) {
//
//        try (
//                FileChannel from = new FileInputStream("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\netty-demo\\data.txt").getChannel();
//                FileChannel to = new FileOutputStream("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\netty-demo\\to.txt").getChannel();
//        ) {
//            // 效率高，底层会利用操作系统的零拷贝进行优化，只能传输小于2g的数据。
//            from.transferTo(0, from.size(), to);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    // 大于2g的数据
    public static void main(String[] args) {
        try (
                FileChannel from = new FileInputStream("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\netty-demo\\data.txt").getChannel();
                FileChannel to = new FileOutputStream("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\netty-demo\\to.txt").getChannel();
        ) {
            // 效率高，底层会利用操作系统的零拷贝进行优化
            long size = from.size();
            // left 变量代表还剩余多少字节
            for (long left = size; left > 0; ) {
                System.out.println("position:" + (size - left) + " left:" + left);
                left -= from.transferTo((size - left), left, to);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
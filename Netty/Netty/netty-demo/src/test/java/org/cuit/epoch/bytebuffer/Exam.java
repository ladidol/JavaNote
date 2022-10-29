package org.cuit.epoch.bytebuffer;

import java.nio.ByteBuffer;

import static org.cuit.epoch.bytebuffer.ByteBufferUtil.debugAll;

/**
 * @author: ladidol
 * @date: 2022/10/29 19:31
 * @description:
 */
public class Exam {

    /*
    网络上有多条数据发送给服务端，数据之间使用 \n 进行分隔
    但由于某种原因这些数据在接收时，被进行了重新组合，例如原始数据有3条为

    * Hello,world\n
    * I'm zhangsan\n
    * How are you?\n

    变成了下面的两个 byteBuffer (黏包，半包)

    * Hello,world\nI'm zhangsan\nHo
    * w are you?\n

    现在要求你编写程序，将错乱的数据恢复成原始的按 \n 分隔的数据
    */
    public static void main(String[] args) {
        ByteBuffer source = ByteBuffer.allocate(64);
        //                     11            24
        source.put("Hello,world\nI'm ladidol\nHo".getBytes());
        split(source);

        source.put("w are you?\nhaha!\n".getBytes());
        split(source);
    }

    private static void split(ByteBuffer source) {
        source.flip();

        for (int i = 0; i < source.limit(); i++) {
            int oldLimit = source.limit();
            //找到一条完整信息
            if (source.get(i) == '\n') {
                //把这条信息读出来。
                int length = i + 1 - source.position();
                // 存入新的ByteBuffer中去
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从 source 读到 target 中去
//                for (int j = 0; j < length; j++) {
//                    target.put(source.get());
//                }
                source.limit(i + 1);
                target.put(source);
                //limit恢复原样
                source.limit(oldLimit);

                debugAll(target);
            }
        }


        source.compact();//从\n未读位置开始读。
    }
}
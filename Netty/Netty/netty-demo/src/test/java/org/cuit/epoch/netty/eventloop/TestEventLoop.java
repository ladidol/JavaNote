package org.cuit.epoch.netty.eventloop;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author: ladidol
 * @date: 2022/11/8 19:57
 * @description:
 */
@Slf4j
public class TestEventLoop {

    public static void main(String[] args) {
        // 1. 创建时间循环组,// 内部创建了两个 EventLoop, 每个 EventLoop 维护一个线程
        EventLoopGroup group = new NioEventLoopGroup(2);// io事件，普通任务，定时任务
//        EventLoopGroup group2 = new DefaultEventLoopGroup(2);// 普通任务，定时任务
        // 2. 获取下一个时间循环对象，可以用for循环来遍历。
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());
        //io.netty.channel.nio.NioEventLoop@27c20538
        //io.netty.channel.nio.NioEventLoop@72d818d1
        //io.netty.channel.nio.NioEventLoop@27c20538

        // 3. 执行普通任务
        group.next().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("你好，普通任务完成了");
            }
        });

        // 4. 执行定时任务
        group.next().scheduleAtFixedRate(()->{
            log.debug("你好，定时任务完成了");
        },0,1, TimeUnit.SECONDS);//第一个是最开始延迟计时时间、间隔时间、时间单位。








        log.debug("main is ok");


    }
}
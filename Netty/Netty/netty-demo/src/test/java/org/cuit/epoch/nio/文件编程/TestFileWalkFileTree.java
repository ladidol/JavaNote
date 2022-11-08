package org.cuit.epoch.nio.文件编程;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: ladidol
 * @date: 2022/11/1 14:52
 * @description:
 */
public class TestFileWalkFileTree {

    public static void main(String[] args) throws IOException {

    }



    /**
     * 参数：[]
     * 返回值：void
     * 作者： ladidol
     * 描述：递归删除文件夹中的文件（夹）
     */
    private static void m3() throws IOException {
        Path path = Paths.get("E:\\delete");
        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                //先进入文件夹再删除里面的文件。
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                //走出文件夹后把文件夹删除掉。
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    /**
     * 参数：[]
     * 返回值：void
     * 作者： ladidol
     * 描述：查询文件夹下有多少个指定类型文件。
     */
    private static void m2() throws IOException {
        Path path = Paths.get("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\");
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                if (file.toFile().getName().endsWith(".java")) {
                    System.out.println(file);
                    fileCount.incrementAndGet();
                }
                return super.visitFile(file, attrs);
            }
        });
        System.out.println(fileCount); // 11
    }


    /**
     * 参数：[]
     * 返回值：void
     * 作者： ladidol
     * 描述：看一下有多少个文件和文件夹
     */
    private static void m1() throws IOException {
        // 并发安全原子计数器，这里不用count的原因不是多线程，是因为匿名内部类只能使用final变量，而这样了count就不能自加了。
        final int count = 0;

        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();

        Files.walkFileTree(Paths.get("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\"), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                System.out.println("=====> " + dir);
                dirCount.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println(file);
                fileCount.incrementAndGet();
                return super.visitFile(file, attrs);
            }

        });

        System.out.println("dirCount = " + dirCount);
        System.out.println("fileCount = " + fileCount);
    }

}
package org.cuit.epoch.nio.文件编程;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author: ladidol
 * @date: 2022/11/1 15:42
 * @description:
 */
public class TestFileCopy {

    public static void main(String[] args) throws IOException {

        String source = "E:\\delete";
        String target = "E:\\delete123";

        Files.walk(Paths.get(source)).forEach(path -> {

            try {
                String targetName = path.toString().replace(source, target);// 得到目标文件的绝对路径
                //是目录
                if (Files.isDirectory(path)) {
                    Files.createDirectory(Paths.get(targetName));
                }
                // 是普通文件
                else if (Files.isRegularFile(path)) {
                    Files.copy(path, Paths.get(targetName));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }

}
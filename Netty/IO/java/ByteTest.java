import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author: ladidol
 * @date: 2022/10/28 22:29
 * @description:
 */
public class ByteTest {


    public static void copyFile(String src, String dist) throws IOException {

        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dist);
        byte[] buffer = new byte[20 * 1024];

        // read() 最多读取 buffer.length 个字节
        // 返回的是实际读取的个数
        // 返回 -1 的时候表示读到 eof，即文件尾
        while (in.read(buffer, 0, buffer.length) != -1) {
            out.write(buffer);
        }

        in.close();
        out.close();
    }

    public static void main(String[] args) throws IOException {

        copyFile("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\IO\\java\\file\\input.txt",
                "E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\IO\\java\\file\\output.txt");
    }




}
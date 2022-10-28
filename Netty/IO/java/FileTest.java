import java.io.File;

/**
 * @author: ladidol
 * @date: 2022/10/28 22:24
 * @description:
 */
public class FileTest {

    public static void listAllFiles(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        if (dir.isFile()) {
            System.out.println(dir.getAbsoluteFile());
            return;
        }
        for (File file : dir.listFiles()) {
            listAllFiles(file);
        }
    }

    public static void main(String[] args) {
        listAllFiles(new File("D:\\B站\\bilibili"));
    }
}

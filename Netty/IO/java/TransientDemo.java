import java.io.*;
import java.util.Arrays;

public class TransientDemo {

    public static void main(String[] args) {

        UserBean user = new UserBean();
        user.setName("ladidol");
        user.setPassword("password123");
        user.setElementData(new String[]{"String1","String2","String3"});

        System.out.println("User: " + user.toString());

        //begin serializing
        try {
            ObjectOutputStream fos = new ObjectOutputStream(new FileOutputStream("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\IO\\java\\file\\bean.txt"));
            fos.writeObject(user);
            fos.flush();
            fos.close();
            System.out.println("local serialized done");
        } catch (Exception e) {
        }

        System.out.println("de-serialzing...");
        try {
            ObjectInputStream fis = new ObjectInputStream(new FileInputStream("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\IO\\java\\file\\bean.txt"));
            user = (UserBean) fis.readObject();
            fis.close();
            System.out.println("User de-serialzed: " + user.toString());
        } catch (Exception e) {
            System.out.println("e = " + e);
        }
    }
    static class UserBean implements Serializable {
        private String name;
        private transient String password;
        private transient String[] elementData;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }


        public String[] getElementData() {
            return elementData;
        }

        public void setElementData(String[] elementData) {
            this.elementData = elementData;
        }

        @Override
        public String toString() {
            return "UserBean{" +
                    "name='" + name + '\'' +
                    ", password='" + password + '\'' +
                    ", elementData=" + Arrays.toString(elementData) +
                    '}';
        }
    }
}


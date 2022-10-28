## IO分类

### IO理解分类-从传输方式

从数据传输方式或者说是运输方式角度看，可以将 IO 类分为:

- 字节流
- 字符流

`字节`是个计算机看的，`字符`才是给人看的

#### 1）字节流

大致如下

![image-20221028214437009](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202210282144193.png)



#### 2）字符流

大致如下

![image-20221028214511704](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202210282145874.png)



#### 3）字节流和字符流的区别

- 字节流读取单个字节，字符流读取单个字符 (一个字符根据编码的不同，对应的字节也不同，如 UTF-8 编码中文汉字是 3 个字节，GBK编码中文汉字是 2 个字节。）
- 字节流用来处理二进制文件(图片、MP3、视频文件)，字符流用来处理文本文件(可以看做是特殊的二进制文件，使用了某种编码，人可以阅读。）

#### 4）字节转字符Input/OutputStreamReader/Writer

编码就是把字符转换为字节，而解码是把字节重新组合成字符。

如果编码和解码过程使用不同的编码方式那么就出现了乱码。

- GBK 编码中，中文字符占 2 个字节，英文字符占 1 个字节；
- UTF-8 编码中，中文字符占 3 个字节，英文字符占 1 个字节；
- UTF-16be 编码中，中文字符和英文字符都占 2 个字节。

Java 使用双字节编码 UTF-16be，这不是指 Java 只支持这一种编码方式，而是说 char 这种类型使用 UTF-16be 进行编码。char 类型占 16 位，也就是两个字节，**Java 使用这种双字节编码是为了让一个中文或者一个英文都能使用一个 char 来存储。**

![image-20221028215430439](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202210282154596.png)



### IO理解分类 - 从数据操作上

从数据来源或者说是操作对象角度看，IO 类可以分为:

![image-20221028220215308](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202210282202477.png)



## IO设计模式(装饰者模式)

### 装饰者模式

装饰者(Decorator)和具体组件(ConcreteComponent)都继承自组件(Component)，具体组件的方法实现不需要依赖于其它对象，而装饰者组合了一个组件，这样它可以装饰其它装饰者或者具体组件。所谓装饰，就是把这个装饰者套在被装饰者之上，从而动态扩展被装饰者的功能。装饰者的方法有一部分是自己的，这属于它的功能，然后调用被装饰者的方法实现，从而也保留了被装饰者的功能。可以看到，具体组件应当是装饰层次的最低层，因为只有具体组件的方法实现不需要依赖于其它对象。

![image-20221028220801915](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202210282208063.png)



### IO装饰者模式

以 InputStream 为例，

- InputStream 是抽象组件；
- FileInputStream 是 InputStream 的子类，属于具体组件，提供了字节流的输入操作；
- FilterInputStream 属于抽象装饰者，装饰者用于装饰组件，为组件提供额外的功能。例如 BufferedInputStream 为 FileInputStream 提供缓存的功能。

![image-20221028220855555](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202210282208698.png)

实例化一个具有缓存功能的字节流对象时，只需要在 FileInputStream 对象上再套一层 BufferedInputStream 对象即可。

```java
FileInputStream fileInputStream = new FileInputStream(filePath);
BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
```

DataInputStream 装饰者提供了对更多数据类型进行输入的操作，比如 int、double 等基本类型。

## IO常用类的使用

Java 的 I/O 大概可以分成以下几类:

- 磁盘操作: File
- 字节操作: InputStream 和 OutputStream
- 字符操作: Reader 和 Writer
- 对象操作: Serializable
- 网络操作: Socket

### 1）File相关

File 类可以用于表示文件和目录的信息，但是它不表示文件的内容。

递归地列出一个目录下所有文件:

```java
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
```

### 2）字节流相关

```java
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
```

### 3）现逐行输出文本文件的内容

```java
public static void readFileContent(String filePath) throws IOException {

    FileReader fileReader = new FileReader(filePath);
    BufferedReader bufferedReader = new BufferedReader(fileReader);

    String line;
    while ((line = bufferedReader.readLine()) != null) {
        System.out.println(line);
    }

    // 装饰者模式使得 BufferedReader 组合了一个 Reader 对象
    // 在调用 BufferedReader 的 close() 方法时会去调用 Reader 的 close() 方法
    // 因此只要一个 close() 调用即可
    bufferedReader.close();
}
```









### 4）序列化 & Serializable & transient

序列化就是将一个对象转换成字节序列，方便存储和传输。

- 序列化: ObjectOutputStream.writeObject()
- 反序列化: ObjectInputStream.readObject()

不会对静态变量进行序列化，因为序列化只是保存对象的状态，静态变量属于类的状态。

**Serializable**

序列化的类需要实现 Serializable 接口，它只是一个标准，没有任何方法需要实现，但是如果不去实现它的话而进行序列化，会抛出异常。



```java
public static void readFileContent(String filePath) throws IOException {

    FileReader fileReader = new FileReader(filePath);
    BufferedReader bufferedReader = new BufferedReader(fileReader);

    String line;
    while ((line = bufferedReader.readLine()) != null) {
        System.out.println(line);
    }

    // 装饰者模式使得 BufferedReader 组合了一个 Reader 对象
    // 在调用 BufferedReader 的 close() 方法时会去调用 Reader 的 close() 方法
    // 因此只要一个 close() 调用即可
    bufferedReader.close();
}
```

**transient**

transient 关键字可以使一些属性不会被序列化。

ArrayList 中存储数据的数组 elementData 是用 transient 修饰的，因为这个数组是动态扩展的，并不是所有的空间都被使用，因此就不需要所有的内容都被序列化。通过重写序列化和反序列化方法，使得可以只序列化数组中有内容的那部分数据。

```java
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
```

### 5）Java 中的网络支持:

- InetAddress: 用于表示网络上的硬件资源，即 IP 地址；
- URL: 统一资源定位符；
- Sockets: 使用 TCP 协议实现网络通信；
- Datagram: 使用 UDP 协议实现网络通信。



#### 1、InetAddress

没有公有的构造函数，只能通过静态方法来创建实例。

```java
InetAddress.getByName(String host);
InetAddress.getByAddress(byte[] address); 
```

#### 2、URL

可以直接从 URL 中读取字节流数据。

#### 3、Sockets

- ServerSocket: 服务器端类
- Socket: 客户端类
- 服务器和客户端通过 InputStream 和 OutputStream 进行输入输出。

![image-20221029005115465](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202210290051536.png)

#### 4、Datagram

- DatagramSocket: 通信类
- DatagramPacket: 数据包类

## 2、常见问题

- Java 字节读取流的read方法返回int的原因

https://blog.csdn.net/congwiny/article/details/18922847
















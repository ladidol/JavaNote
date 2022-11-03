non-blocking io 非阻塞 IO

## 1. 三大组件

### 1.1 Channel & Buffer

channel 有一点类似于 stream，它就是读写数据的**双向通道**，可以从 channel 将数据读入 buffer，也可以将 buffer 的数据写入 channel，而之前的 stream 要么是输入，要么是输出，channel 比 stream 更为底层

```mermaid
graph LR
channel --> buffer
buffer --> channel
```

**常见的 Channel 有**

* FileChannel
* DatagramChannel
* SocketChannel
* ServerSocketChannel

buffer 则用来缓冲读写数据，**常见的 buffer 有**

* ByteBuffer
  * MappedByteBuffer
  * DirectByteBuffer
  * HeapByteBuffer
* ShortBuffer
* IntBuffer
* LongBuffer
* FloatBuffer
* DoubleBuffer
* CharBuffer

### 1.2 Selector

selector 单从字面意思不好理解，需要结合服务器的设计演化来理解它的用途

#### 多线程版设计

```mermaid
graph TD
subgraph 多线程版
t1(thread) --> s1(socket1)
t2(thread) --> s2(socket2)
t3(thread) --> s3(socket3)
end
```

#### ⚠️ 多线程版缺点

* 当线程多起来了，内存占用高
* 线程上下文切换成本高
* 只适合连接数少的场景

#### 线程池版设计

```mermaid
graph TD
subgraph 线程池版
t4(thread) --> s4(socket1)
t5(thread) --> s5(socket2)
t4(thread) -.-> s6(socket3)
t5(thread) -.-> s7(socket4)
end
```

#### ⚠️ 线程池版缺点

* 阻塞模式下，一个线程依旧仅能处理一个 socket 连接
* 仅适合短连接场景，快速断开socket的场景

#### selector 版设计

selector 的作用就是配合一个线程来管理多个 channel，获取这些 channel 上发生的事件，这些 channel 工作在非阻塞模式下，不会让线程吊死在一个 channel 上。适合连接数特别多，但流量低的场景（low traffic）

```mermaid
graph TD
subgraph selector 版
thread --> selector
selector --> c1(channel)
selector --> c2(channel)
selector --> c3(channel)
end
```

调用 selector 的 select() 会阻塞直到 channel 发生了读写就绪事件，这些事件发生，select 方法就会返回这些事件交给 thread 来处理

**（把线程thread比作服务员，selector比作监视器来监视每一个被负责的客户需求，channel比作被负责的顾客。）**

## 2. ByteBuffer

准备一个普通文本文件data.txt，内容为

```
1234567890abc
```

使用FileChannel来读取文件内容

```java
@Slf4j
public class TestByteBuffer {

    public static void main(String[] args) {
        //FileChannel
        //1. 输入输出流， 2. RandomAccessFile
        //通过try-with-resource能把对文件资源的释放
        try (FileChannel channel = new FileInputStream("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\netty-demo\\data.txt").getChannel()) {
            //准备一个缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);

//            // 从channel中读取数据， 先向 缓冲区buffer 写入
//            channel.read(buffer);
//            // 打印 buffer 的内容
//            buffer.flip();// 切换至读模式
//
//            while (buffer.hasRemaining()){// 是否有剩余的未读数据
//                byte b = buffer.get();//读一个字节
//                System.out.println((char) b);
//            }
//
//            //这里buffer只有10空间，所以一次读取只会从文件中读取10个字节。

            while (true) {
                // 先向 缓冲区buffer 写入
                int len = channel.read(buffer);
                log.debug("读取到的字节数：{}", len);
                if (len == -1) {//没有内容了
                    break;
                }
                // 切换 buffer 至读模式
                buffer.flip();
                while (buffer.hasRemaining()) {// 是否有剩余的未读数据
                    byte b = buffer.get();//读一个字节
                    log.debug("{}", (char) b);
                }
                //切换 buffer 至写模式
                buffer.clear();
            }


        } catch (IOException e) {
            System.out.println("e = " + e);
        }
    }
}
```

日志输出：

```
[DEBUG] 16:54:19.536 [main] o.c.e.TestByteBuffer - 读取到的字节数：10 
[DEBUG] 16:54:19.546 [main] o.c.e.TestByteBuffer - 1 
[DEBUG] 16:54:19.546 [main] o.c.e.TestByteBuffer - 2 
[DEBUG] 16:54:19.546 [main] o.c.e.TestByteBuffer - 3 
[DEBUG] 16:54:19.546 [main] o.c.e.TestByteBuffer - 4 
[DEBUG] 16:54:19.547 [main] o.c.e.TestByteBuffer - 5 
[DEBUG] 16:54:19.547 [main] o.c.e.TestByteBuffer - 6 
[DEBUG] 16:54:19.547 [main] o.c.e.TestByteBuffer - 7 
[DEBUG] 16:54:19.547 [main] o.c.e.TestByteBuffer - 8 
[DEBUG] 16:54:19.547 [main] o.c.e.TestByteBuffer - 9 
[DEBUG] 16:54:19.547 [main] o.c.e.TestByteBuffer - 0 
[DEBUG] 16:54:19.547 [main] o.c.e.TestByteBuffer - 读取到的字节数：3 
[DEBUG] 16:54:19.547 [main] o.c.e.TestByteBuffer - a 
[DEBUG] 16:54:19.547 [main] o.c.e.TestByteBuffer - b 
[DEBUG] 16:54:19.547 [main] o.c.e.TestByteBuffer - c 
[DEBUG] 16:54:19.547 [main] o.c.e.TestByteBuffer - 读取到的字节数：-1 
```



### 2.1  ByteBuffer 正确使用姿势

1. 向 buffer 写入数据，例如调用 channel.read(buffer)
2. 调用 flip() 切换至**读模式**
3. 从 buffer 读取数据，例如调用 buffer.get()
4. 调用 clear() 或 compact() 切换至**写模式**
5. 重复 1~4 步骤



### 2.2 ByteBuffer 结构

ByteBuffer 有以下重要属性

* capacity
* position
* limit

一开始

![](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202210291702028.png)

写模式下，position 是写入位置，limit 等于容量，下图表示写入了 4 个字节后的状态

![](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202210291702041.png)

flip 动作发生后，position 切换为读取位置，limit 切换为读取限制

![](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202210291702045.png)

读取 4 个字节后，状态

![](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202210291702037.png)

clear 动作发生后，状态

![](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202210291702059.png)

compact 方法，是把未读完的部分向前压缩，然后切换至写模式

> cd未读，就把cd放到前面来，写模式就直接从后面开始写。

![](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202210291702068.png)



#### 💡 调试工具类

```java
package org.cuit.epoch.bytebuffer;

import io.netty.util.internal.StringUtil;

import java.nio.ByteBuffer;

import static io.netty.util.internal.MathUtil.isOutOfBounds;
import static jdk.nashorn.internal.runtime.regexp.joni.encoding.CharacterType.NEWLINE;


/**
 * 作者： ladidol
 * 描述：看一下缓存的状态，缓存以十六进制进行存储。
 */
public class ByteBufferUtil {
    private static final char[] BYTE2CHAR = new char[256];
    private static final char[] HEXDUMP_TABLE = new char[256 * 4];
    private static final String[] HEXPADDING = new String[16];
    private static final String[] HEXDUMP_ROWPREFIXES = new String[65536 >>> 4];
    private static final String[] BYTE2HEX = new String[256];
    private static final String[] BYTEPADDING = new String[16];

    static {
        final char[] DIGITS = "0123456789abcdef".toCharArray();
        for (int i = 0; i < 256; i++) {
            HEXDUMP_TABLE[i << 1] = DIGITS[i >>> 4 & 0x0F];
            HEXDUMP_TABLE[(i << 1) + 1] = DIGITS[i & 0x0F];
        }

        int i;

        // Generate the lookup table for hex dump paddings
        for (i = 0; i < HEXPADDING.length; i++) {
            int padding = HEXPADDING.length - i;
            StringBuilder buf = new StringBuilder(padding * 3);
            for (int j = 0; j < padding; j++) {
                buf.append("   ");
            }
            HEXPADDING[i] = buf.toString();
        }

        // Generate the lookup table for the start-offset header in each row (up to 64KiB).
        for (i = 0; i < HEXDUMP_ROWPREFIXES.length; i++) {
            StringBuilder buf = new StringBuilder(12);
            buf.append(NEWLINE);
            buf.append(Long.toHexString(i << 4 & 0xFFFFFFFFL | 0x100000000L));
            buf.setCharAt(buf.length() - 9, '|');
            buf.append('|');
            HEXDUMP_ROWPREFIXES[i] = buf.toString();
        }

        // Generate the lookup table for byte-to-hex-dump conversion
        for (i = 0; i < BYTE2HEX.length; i++) {
            BYTE2HEX[i] = ' ' + StringUtil.byteToHexStringPadded(i);
        }

        // Generate the lookup table for byte dump paddings
        for (i = 0; i < BYTEPADDING.length; i++) {
            int padding = BYTEPADDING.length - i;
            StringBuilder buf = new StringBuilder(padding);
            for (int j = 0; j < padding; j++) {
                buf.append(' ');
            }
            BYTEPADDING[i] = buf.toString();
        }

        // Generate the lookup table for byte-to-char conversion
        for (i = 0; i < BYTE2CHAR.length; i++) {
            if (i <= 0x1f || i >= 0x7f) {
                BYTE2CHAR[i] = '.';
            } else {
                BYTE2CHAR[i] = (char) i;
            }
        }
    }

    /**
     * 打印所有内容
     * @param buffer
     */
    public static void debugAll(ByteBuffer buffer) {
        int oldlimit = buffer.limit();
        buffer.limit(buffer.capacity());
        StringBuilder origin = new StringBuilder(256);
        appendPrettyHexDump(origin, buffer, 0, buffer.capacity());
        System.out.println("+--------+-------------------- all ------------------------+----------------+");
        System.out.printf("position: [%d], limit: [%d]\n", buffer.position(), oldlimit);
        System.out.println(origin);
        buffer.limit(oldlimit);
    }

    /**
     * 打印可读取内容
     * @param buffer
     */
    public static void debugRead(ByteBuffer buffer) {
        StringBuilder builder = new StringBuilder(256);
        appendPrettyHexDump(builder, buffer, buffer.position(), buffer.limit() - buffer.position());
        System.out.println("+--------+-------------------- read -----------------------+----------------+");
        System.out.printf("position: [%d], limit: [%d]\n", buffer.position(), buffer.limit());
        System.out.println(builder);
    }

    private static void appendPrettyHexDump(StringBuilder dump, ByteBuffer buf, int offset, int length) {
        if (isOutOfBounds(offset, length, buf.capacity())) {
            throw new IndexOutOfBoundsException(
                    "expected: " + "0 <= offset(" + offset + ") <= offset + length(" + length
                            + ") <= " + "buf.capacity(" + buf.capacity() + ')');
        }
        if (length == 0) {
            return;
        }
        dump.append(
                "         +-------------------------------------------------+\n" +
                        NEWLINE + "         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |\n" +
                        NEWLINE + "+--------+-------------------------------------------------+----------------+\n");

        final int startIndex = offset;
        final int fullRows = length >>> 4;
        final int remainder = length & 0xF;

        // Dump the rows which have 16 bytes.
        for (int row = 0; row < fullRows; row++) {
            int rowStartIndex = (row << 4) + startIndex;

            // Per-row prefix.
            appendHexDumpRowPrefix(dump, row, rowStartIndex);

            // Hex dump
            int rowEndIndex = rowStartIndex + 16;
            for (int j = rowStartIndex; j < rowEndIndex; j++) {
                dump.append(BYTE2HEX[getUnsignedByte(buf, j)]);
            }
            dump.append(" |");

            // ASCII dump
            for (int j = rowStartIndex; j < rowEndIndex; j++) {
                dump.append(BYTE2CHAR[getUnsignedByte(buf, j)]);
            }
            dump.append('|');
        }

        // Dump the last row which has less than 16 bytes.
        if (remainder != 0) {
            int rowStartIndex = (fullRows << 4) + startIndex;
            appendHexDumpRowPrefix(dump, fullRows, rowStartIndex);

            // Hex dump
            int rowEndIndex = rowStartIndex + remainder;
            for (int j = rowStartIndex; j < rowEndIndex; j++) {
                dump.append(BYTE2HEX[getUnsignedByte(buf, j)]);
            }
            dump.append(HEXPADDING[remainder]);
            dump.append(" |");

            // Ascii dump
            for (int j = rowStartIndex; j < rowEndIndex; j++) {
                dump.append(BYTE2CHAR[getUnsignedByte(buf, j)]);
            }
            dump.append(BYTEPADDING[remainder]);
            dump.append('|');
        }

        dump.append("\n"+NEWLINE +
                "+--------+-------------------------------------------------+----------------+");
    }

    private static void appendHexDumpRowPrefix(StringBuilder dump, int row, int rowStartIndex) {
        if (row < HEXDUMP_ROWPREFIXES.length) {
            dump.append(HEXDUMP_ROWPREFIXES[row]);
        } else {
            dump.append(NEWLINE);
            dump.append(Long.toHexString(rowStartIndex & 0xFFFFFFFFL | 0x100000000L));
            dump.setCharAt(dump.length() - 9, '|');
            dump.append('|');
        }
    }

    public static short getUnsignedByte(ByteBuffer buffer, int index) {
        return (short) (buffer.get(index) & 0xFF);
    }
}
```

示例控制台输出：

```
+--------+-------------------- all ------------------------+----------------+
position: [0], limit: [10]
         +-------------------------------------------------+
0         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
0+--------+-------------------------------------------------+----------------+
0|00000000| 00 00 00 00 00 00 00 00 00 00                   |..........      |
0+--------+-------------------------------------------------+----------------+
```

### 2.3 ByteBuffer 常见方法

#### 分配空间

可以使用 allocate 方法为 ByteBuffer 分配空间，其它 buffer 类也有该方法

```java
Bytebuffer buf = ByteBuffer.allocate(16);
```



#### 向 buffer 写入数据

有两种办法

* 调用 channel 的 read 方法
* 调用 buffer 自己的 put 方法

```java
int readBytes = channel.read(buf);
```

和

```java
buf.put((byte)127);
```



#### 从 buffer 读取数据

同样有两种办法

* 调用 channel 的 write 方法
* 调用 buffer 自己的 get 方法

```java
int writeBytes = channel.write(buf);
```

和

```java
byte b = buf.get();
```

get 方法会让 position 读指针向后走，如果想重复读取数据

* 可以调用 rewind 方法将 position 重新置为 0
* 或者调用 get(int i) 方法获取索引 i 的内容，它不会移动读指针

#### mark 和 reset

mark 是在读取时，做一个标记，即使 position 改变，只要调用 reset 就能回到 mark 的位置

> **注意**
>
> rewind 和 flip 都会清除 mark 位置

详细代码可以看仓库中的代码文件。

#### 字符串与 ByteBuffer 互转

```java
// 1. 字符串转为 ByteBuffer
ByteBuffer buffer = ByteBuffer.allocate(16);
buffer.put("hello ladidol".getBytes());
debugAll(buffer);

// 2. Charset
ByteBuffer hello_ladidol = StandardCharsets.UTF_8.encode("hello ladidol");
debugAll(hello_ladidol);//用这种方法会自动转换成读模式

// 3. wrap
ByteBuffer wrap = ByteBuffer.wrap("hello ladidol".getBytes());
debugAll(wrap);

// buffer to String
String s = StandardCharsets.UTF_8.decode(hello_ladidol).toString();
System.out.println("s = " + s);//注意buffer要切换成读模式。
```

输出：

```
+--------+-------------------- all ------------------------+----------------+
position: [13], limit: [16]
         +-------------------------------------------------+
0         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
0+--------+-------------------------------------------------+----------------+
0|00000000| 68 65 6c 6c 6f 20 6c 61 64 69 64 6f 6c 00 00 00 |hello ladidol...|
0+--------+-------------------------------------------------+----------------+
+--------+-------------------- all ------------------------+----------------+
position: [0], limit: [13]
         +-------------------------------------------------+
0         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
0+--------+-------------------------------------------------+----------------+
0|00000000| 68 65 6c 6c 6f 20 6c 61 64 69 64 6f 6c 00       |hello ladidol.  |
0+--------+-------------------------------------------------+----------------+
+--------+-------------------- all ------------------------+----------------+
position: [0], limit: [13]
         +-------------------------------------------------+
0         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
0+--------+-------------------------------------------------+----------------+
0|00000000| 68 65 6c 6c 6f 20 6c 61 64 69 64 6f 6c          |hello ladidol   |
0+--------+-------------------------------------------------+----------------+
s = hello ladidol
```



#### ⚠️ Buffer 的线程不安全

> Buffer 是**非线程安全的**



### 2.4 Scattering Reads

分散读取，有一个文本文件 3parts.txt

```java
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
```





### 2.5 Gathering Writes

使用如下方式写入，可以将多个 buffer 的数据填充至 channel

```java
public class TestGatheringReads {
    public static void main(String[] args) {
        try (RandomAccessFile file = new RandomAccessFile("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\netty-demo\\3parts.txt", "rw")) {
            FileChannel channel = file.getChannel();
            ByteBuffer d = ByteBuffer.allocate(4);
            ByteBuffer e = ByteBuffer.allocate(4);
            channel.position(11);//跳过字符onetwothree

            d.put(new byte[]{'f', 'o', 'u', 'r'});
            e.put(new byte[]{'f', 'i', 'v', 'e'});
            d.flip();//写入文件之前也要flip一下。
            e.flip();
            debugAll(d);
            debugAll(e);
            channel.write(new ByteBuffer[]{d, e});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //+--------+-------------------- all ------------------------+----------------+
    //position: [0], limit: [4]
    //         +-------------------------------------------------+
    //0         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
    //0+--------+-------------------------------------------------+----------------+
    //0|00000000| 66 6f 75 72                                     |four            |
    //0+--------+-------------------------------------------------+----------------+
    //+--------+-------------------- all ------------------------+----------------+
    //position: [0], limit: [4]
    //         +-------------------------------------------------+
    //0         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
    //0+--------+-------------------------------------------------+----------------+
    //0|00000000| 66 69 76 65                                     |five            |
    //0+--------+-------------------------------------------------+----------------+
}
```

### 2.6 练习

网络上有多条数据发送给服务端，数据之间使用 \n 进行分隔
但由于某种原因这些数据在接收时，被进行了重新组合，例如原始数据有3条为

* Hello,world\n
* I'm zhangsan\n
* How are you?\n

变成了下面的两个 byteBuffer (黏包，半包)

* Hello,world\nI'm zhangsan\nHo
* w are you?\n

现在要求你编写程序，将错乱的数据恢复成原始的按 \n 分隔的数据

```java
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
```



## 3. 文件编程

### 3.1 FileChannel

#### ⚠️ FileChannel 工作模式

> FileChannel 只能工作在阻塞模式下，所以不能使用selector



#### 获取

不能直接打开 FileChannel，必须通过 FileInputStream、FileOutputStream 或者 RandomAccessFile 来获取 FileChannel，它们都有 getChannel 方法

* 通过 FileInputStream 获取的 channel 只能读
* 通过 FileOutputStream 获取的 channel 只能写
* 通过 RandomAccessFile 是否能读写根据构造 RandomAccessFile 时的读写模式决定



#### 读取

会从 channel 读取数据填充 ByteBuffer，返回值表示读到了多少字节，-1 表示到达了文件的末尾

```java
int readBytes = channel.read(buffer);
```



#### 写入

写入的正确姿势如下， SocketChannel

```java
ByteBuffer buffer = ...;
buffer.put(...); // 存入数据
buffer.flip();   // 切换读模式

while(buffer.hasRemaining()) {
    channel.write(buffer);
}
```

在 while 中调用 channel.write 是因为 write 方法并不能保证一次将 buffer 中的内容全部写入 channel



#### 关闭

channel 必须关闭，不过调用了 FileInputStream、FileOutputStream 或者 RandomAccessFile 的 close 方法会间接地调用 channel 的 close 方法



#### 位置

获取当前位置

```java
long pos = channel.position();
```

设置当前位置

```java
long newPos = ...;
channel.position(newPos);
```

设置当前位置时，如果设置为文件的末尾

* 这时读取会返回 -1 
* 这时写入，会追加内容，但要注意如果 position 超过了文件末尾，再写入时在新内容和原末尾之间会有空洞（00）



#### 大小

使用 size 方法获取文件的大小



#### 强制写入

操作系统出于性能的考虑，会将数据缓存，不是立刻写入磁盘。可以调用 force(true)  方法将文件内容和元数据（文件的权限等信息）立刻写入磁盘

### 3.2 两个 Channel 传输数据

```java
// 小于2g的数据，直接转换就行
public static void main(String[] args) {

    try (
        FileChannel from = new FileInputStream("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\netty-demo\\data.txt").getChannel();
        FileChannel to = new FileOutputStream("E:\\Java\\ladidol\\ladidol_JavaNote\\Netty\\Netty\\netty-demo\\to.txt").getChannel();
    ) {
        // 效率高，底层会利用操作系统的零拷贝进行优化，只能传输小于2g的数据。
        from.transferTo(0, from.size(), to);
    } catch (IOException e) {
        e.printStackTrace();
    }

}
```



超过 2g 大小的文件传输

```java
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
```

实际传输一个超大文件

```
position:0 left:7769948160
position:2147483647 left:5622464513
position:4294967294 left:3474980866
position:6442450941 left:1327497219
```



### 3.3 Path

jdk7 引入了 Path 和 Paths 类

* Path 用来表示文件路径
* Paths 是工具类，用来获取 Path 实例

```java
Path source = Paths.get("1.txt"); // 相对路径 使用 user.dir 环境变量来定位 1.txt

Path source = Paths.get("d:\\1.txt"); // 绝对路径 代表了  d:\1.txt

Path source = Paths.get("d:/1.txt"); // 绝对路径 同样代表了  d:\1.txt

Path projects = Paths.get("d:\\data", "projects"); // 代表了  d:\data\projects
```

* `.` 代表了当前路径
* `..` 代表了上一级路径

例如目录结构如下

```
d:
	|- data
		|- projects
			|- a
			|- b
```

代码

```java
Path path = Paths.get("d:\\data\\projects\\a\\..\\b");//找到a的父文件夹作为b的父文件夹。
System.out.println(path);
System.out.println(path.normalize()); // 正常化路径
```

会输出

```
d:\data\projects\a\..\b
d:\data\projects\b
```



### 3.4 Files

检查文件是否存在

```java
Path path = Paths.get("helloword/data.txt");
System.out.println(Files.exists(path));
```



创建一级目录

```java
Path path = Paths.get("helloword/d1");
Files.createDirectory(path);
```

* 如果目录已存在，会抛异常 FileAlreadyExistsException
* 不能一次创建多级目录，否则会抛异常 NoSuchFileException



创建多级目录用

```java
Path path = Paths.get("helloword/d1/d2");
Files.createDirectories(path);
```



拷贝文件

```java
Path source = Paths.get("helloword/data.txt");
Path target = Paths.get("helloword/target.txt");

Files.copy(source, target);
```

* 如果文件已存在，会抛异常 FileAlreadyExistsException

如果希望用 source 覆盖掉 target，需要用 StandardCopyOption 来控制

```java
Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
```



移动文件

```java
Path source = Paths.get("helloword/data.txt");
Path target = Paths.get("helloword/data.txt");

Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
```

* StandardCopyOption.ATOMIC_MOVE 保证文件移动的原子性



删除文件

```java
Path target = Paths.get("helloword/target.txt");

Files.delete(target);
```

* 如果文件不存在，会抛异常 NoSuchFileException



删除目录

```java
Path target = Paths.get("helloword/d1");

Files.delete(target);
```

* 如果目录还有内容，会抛异常 DirectoryNotEmptyException



遍历目录文件

```java
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
```



统计 java 文件的数目

```java
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
```



删除多级目录

```java
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
```









#### ⚠️ 删除很危险

> 删除是危险操作，确保要递归删除的文件夹没有重要内容



拷贝多级目录

```java
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
```





## 4. 网络编程

### 4.1 非阻塞 vs 阻塞

#### 阻塞

* 阻塞模式下，相关方法都会导致线程暂停
  * ServerSocketChannel.accept 会在没有连接建立时让线程暂停
  * SocketChannel.read 会在没有数据可读时让线程暂停
  * 阻塞的表现其实就是线程暂停了，暂停期间不会占用 cpu，但线程相当于闲置
* 单线程下，阻塞方法之间相互影响，几乎不能正常工作，需要多线程支持
* 但多线程下，有新的问题，体现在以下方面
  * 32 位 jvm 一个线程 320k，64 位 jvm 一个线程 1024k，如果连接数过多，必然导致 OOM，并且线程太多，反而会因为频繁上下文切换导致性能降低
  * 可以采用线程池技术来减少线程数和线程上下文切换，但治标不治本，如果有很多连接建立，但长时间 inactive，会阻塞线程池中所有线程，因此不适合长连接，只适合短连接



服务器端

```java
/**
     * 参数：[]
     * 返回值：void
     * 作者： ladidol
     * 描述：单线程的阻塞模式
     */
private static void m1() throws IOException {
    // 使用nio 来理解阻塞模式,这里特地用的单线程来实现。
    // 0. ByteBuffer
    ByteBuffer buffer = ByteBuffer.allocate(16);


    // 1. 创建服务器。
    ServerSocketChannel ssc = ServerSocketChannel.open();


    // 2. 绑定监听端口
    ssc.bind(new InetSocketAddress(8088));


    // 3. 连接集合
    List<SocketChannel> channels = new ArrayList<>();

    while (true) {
        // 4. accept 建立于客户端建立连接，SocketChannel 用来于客户端通信
        log.debug("建立新链接ing");
        SocketChannel sc = ssc.accept();
        channels.add(sc);
        log.debug("已经建立新链接 {}", sc);

        // 5. 接收全部客户端发送的数据
        for (SocketChannel channel : channels) {

            log.debug("before read ...{}", sc);
            channel.read(buffer);
            buffer.flip();
            debugRead(buffer);
            buffer.clear();
            log.debug("after read ...{}", sc);
        }
    }
}
```

客户端

```java
public static void main(String[] args) throws IOException {
    SocketChannel sc = SocketChannel.open();
    sc.connect(new InetSocketAddress("localhost", 8088));
    sc.write(Charset.defaultCharset().encode("hello! ladidol!"));
    System.out.println("waiting...");
    Scanner scan = new Scanner(System.in);
    int n = scan.nextInt();
}
```



#### 非阻塞

* 非阻塞模式下，相关方法都会不会让线程暂停
  * 在 ServerSocketChannel.accept 在没有连接建立时，会返回 null，继续运行
  * SocketChannel.read 在没有数据可读时，会返回 0，但线程不必阻塞，可以去执行其它 SocketChannel 的 read 或是去执行 ServerSocketChannel.accept 
  * 写数据时，线程只是等待数据写入 Channel 即可，无需等 Channel 通过网络把数据发送出去
* 但非阻塞模式下，即使没有连接建立，和可读数据，线程仍然在不断运行，白白浪费了 cpu
* 数据复制过程中，线程实际还是阻塞的（AIO 改进的地方）



服务器端，客户端代码不变

```java
public static void main(String[] args) throws IOException {

    // 使用 nio 来理解非阻塞模式, 单线程
    // 0. ByteBuffer
    ByteBuffer buffer = ByteBuffer.allocate(16);
    // 1. 创建了服务器，设置为非阻塞模式
    ServerSocketChannel ssc = ServerSocketChannel.open();
    ssc.configureBlocking(false); // 非阻塞模式
    // 2. 绑定监听端口
    ssc.bind(new InetSocketAddress(8088));
    // 3. 连接集合
    List<SocketChannel> channels = new ArrayList<>();
    while (true) {
        // 4. accept 建立与客户端连接， SocketChannel 用来与客户端之间通信
        SocketChannel sc = ssc.accept(); // 非阻塞，线程还会继续运行，如果没有连接建立，但sc是null
        if (sc != null) {
            log.debug("connected... {}", sc);
            sc.configureBlocking(false); // 非阻塞模式
            channels.add(sc);
        }
        for (SocketChannel channel : channels) {
            // 5. 接收客户端发送的数据
            int read = channel.read(buffer);// 非阻塞，线程仍然会继续运行，如果没有读到数据，read 返回 0
            if (read > 0) {
                buffer.flip();
                debugRead(buffer);
                buffer.clear();
                log.debug("after read...{}", channel);
            }
        }
    }

}
```



#### 多路复用

单线程可以配合 Selector 完成对多个 Channel 可读写事件的监控，这称之为多路复用

* 多路复用仅针对网络 IO、普通文件 IO 没法利用多路复用
* 如果不用 Selector 的非阻塞模式，线程大部分时间都在做无用功，而 Selector 能够保证
  * 有可连接事件时才去连接
  * 有可读事件才去读取
  * 有可写事件才去写入
    * 限于网络传输能力，Channel 未必时时可写，一旦 Channel 可写，会触发 Selector 的可写事件





### 4.2 Selector



```mermaid
graph TD
subgraph selector 版
thread --> selector
selector --> c1(channel)
selector --> c2(channel)
selector --> c3(channel)
end
```

好处

* 一个线程配合 selector 就可以监控多个 channel 的事件，事件发生线程才去处理。避免非阻塞模式下所做无用功
* 让这个线程能够被充分利用
* 节约了线程的数量
* 减少了线程上下文切换



#### selector创建

```java
Selector selector = Selector.open();
```

#### 绑定注册 Channel 事件

```java
ssc.configureBlocking(false); // 非阻塞模式

//2. 建立selector和channel的联系（注册）
//SelectionKey 就是将来时间发生后，通过他可以知道时间和那个channel的事件。
SelectionKey ssckey = ssc.register(selector, 0, null);
```

* channel 必须工作在非阻塞模式
* FileChannel 没有非阻塞模式，因此不能配合 selector 一起使用
* 绑定的事件类型可以有
  * connect - 客户端连接成功时触发
  * accept - 服务器端成功接受连接时触发
  * read - 数据可读入时触发，有因为接收能力弱，数据暂不能读入的情况
  * write - 数据可写出时触发，有因为发送能力弱，数据暂不能写出的情况

#### 监听 Channel 事件

可以通过下面三种方法来监听是否有事件发生，方法的返回值代表有多少 channel 发生了事件

方法1，阻塞直到绑定事件发生

```java
int count = selector.select();
```



方法2，阻塞直到绑定事件发生，或是超时（时间单位为 ms）

```java
int count = selector.select(long timeout);
```



方法3，不会阻塞，也就是不管有没有事件，立刻返回，自己根据返回值检查是否有事件

```java
int count = selector.selectNow();
```



#### 💡 select 何时不阻塞

> * 事件发生时
>   * 客户端发起连接请求，会触发 accept 事件
>   * 客户端发送数据过来，客户端正常、异常关闭时，都会触发 read 事件，另外如果发送的数据大于 buffer 缓冲区，会触发多次读取事件
>   * channel 可写，会触发 write 事件
>   * 在 linux 下 nio bug 发生时
> * 调用 selector.wakeup()
> * 调用 selector.close()
> * selector 所在线程 interrupt



### 4.3 处理 accept 事件

客户端代码为

```java
public static void main(String[] args) {
    try (Socket socket = new Socket("localhost", 8088)) {
        System.out.println(socket);
        socket.getOutputStream().write("world".getBytes());
        System.in.read();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```



服务器端代码为

```java
    public static void main(String[] args) throws IOException {

        // 1. 建立selector，管理多规格channel
        Selector selector = Selector.open();


        ByteBuffer buffer = ByteBuffer.allocate(16);
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); // 非阻塞模式

        //2. 建立selector和channel的联系（注册）
        //SelectionKey 就是将来时间发生后，通过他可以知道时间和那个channel的事件。
        SelectionKey ssckey = ssc.register(selector, 0, null);
        // key只关注accept事件
        ssckey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("register key:{}", ssckey);

        ssc.bind(new InetSocketAddress(8088));
        List<SocketChannel> channels = new ArrayList<>();
        while (true) {
            //3. select 方法,没有事件发生，线程阻塞；有事件，线程才会恢复运行
            selector.select();

            //4. 处理事件
            //用迭代器可以边遍历边删除set中的元素。
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                log.debug("key: {}", key);
                SelectableChannel channelInKey = key.channel();
                ServerSocketChannel channel = (ServerSocketChannel) channelInKey;
                SocketChannel sc = channel.accept();
                log.debug("{}", sc);
            }
        }
    }
```



#### 💡 事件发生后能否不处理

> 事件发生后，要么处理，要么取消（cancel），不能什么都不做，否则下次该事件仍会触发，这是因为 nio 底层使用的是水平触发

```java
key.cancel();//取消事件
or
channel.accept();//处理事件
```





### 4.4 处理 read 事件

```java
public static void main(String[] args) throws IOException {
    ServerSocketChannel channel = ServerSocketChannel.open();
    channel.bind(new InetSocketAddress(8088));
    System.out.println(channel);
    Selector selector = Selector.open();
    channel.configureBlocking(false);
    SelectionKey ssckey = channel.register(selector, SelectionKey.OP_ACCEPT);//对服务器Channel使用accept事件监听。
    log.debug("register key:{}", ssckey);

    while (true) {
        int count = selector.select();
        //                int count = selector.selectNow();
        log.debug("select count: {}", count);//收到一个事件
        //                if(count <= 0) {
        //                    continue;
        //                }

        // 获取所有事件
        Set<SelectionKey> keys = selector.selectedKeys();

        // 遍历所有事件，逐一处理
        Iterator<SelectionKey> iter = keys.iterator();
        while (iter.hasNext()) {
            SelectionKey key = iter.next();
            log.debug("当前事件key: {}", key);
            log.debug("keys.size() = " + keys.size());

            // 判断事件类型
            if (key.isAcceptable()) {
                ServerSocketChannel c = (ServerSocketChannel) key.channel();
                // 必须处理
                SocketChannel sc = c.accept();
                sc.configureBlocking(false);
                sc.register(selector, SelectionKey.OP_READ);//对收到的普通socketChannel监听read事件
                log.debug("连接已建立: {}", sc);
            } else if (key.isReadable()) {
                try {
                    SocketChannel sc = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(128);
                    int read = sc.read(buffer);
                    if (read == -1) {
                        key.cancel();//这里表示是客户端通过sc.close()，正常断开，read值是-1，因此可以使用 key 取消。
                        sc.close();
                    } else {
                        buffer.flip();
                        debugRead(buffer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    key.cancel();//因为客户端异常断开了（就是直接强制红stop），因此需要将 key 取消。
                }
            }
            // 处理完毕，必须将事件从selectedKeys中移除
            iter.remove();
        }
    }

}
```

开启两个客户端，发送一下文字，输出

```
sun.nio.ch.ServerSocketChannelImpl[/0:0:0:0:0:0:0:0:8088]
[DEBUG] 16:40:34.647 [main] o.c.e.网.单.SelectorServer - register key:sun.nio.ch.SelectionKeyImpl@129a8472 
[DEBUG] 16:40:49.527 [main] o.c.e.网.单.SelectorServer - select count: 1 
[DEBUG] 16:40:49.528 [main] o.c.e.网.单.SelectorServer - 当前事件key: sun.nio.ch.SelectionKeyImpl@129a8472 
[DEBUG] 16:40:49.528 [main] o.c.e.网.单.SelectorServer - keys.size() = 1 
[DEBUG] 16:40:49.528 [main] o.c.e.网.单.SelectorServer - 连接已建立: java.nio.channels.SocketChannel[connected local=/127.0.0.1:8088 remote=/127.0.0.1:2104] 
[DEBUG] 16:40:49.528 [main] o.c.e.网.单.SelectorServer - select count: 1 
[DEBUG] 16:40:49.529 [main] o.c.e.网.单.SelectorServer - 当前事件key: sun.nio.ch.SelectionKeyImpl@3ac3fd8b 
[DEBUG] 16:40:49.529 [main] o.c.e.网.单.SelectorServer - keys.size() = 1 
[DEBUG] 16:40:49.547 [main] i.n.u.i.l.InternalLoggerFactory - Using SLF4J as the default logging framework 
+--------+-------------------- read -----------------------+----------------+
position: [0], limit: [5]
         +-------------------------------------------------+
0         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
0+--------+-------------------------------------------------+----------------+
0|00000000| 68 65 6c 6c 6f                                  |hello           |
0+--------+-------------------------------------------------+----------------+
[DEBUG] 16:41:01.423 [main] o.c.e.网.单.SelectorServer - select count: 1 
[DEBUG] 16:41:01.423 [main] o.c.e.网.单.SelectorServer - 当前事件key: sun.nio.ch.SelectionKeyImpl@129a8472 
[DEBUG] 16:41:01.423 [main] o.c.e.网.单.SelectorServer - keys.size() = 1 
[DEBUG] 16:41:01.424 [main] o.c.e.网.单.SelectorServer - 连接已建立: java.nio.channels.SocketChannel[connected local=/127.0.0.1:8088 remote=/127.0.0.1:2109] 
[DEBUG] 16:41:01.424 [main] o.c.e.网.单.SelectorServer - select count: 1 
[DEBUG] 16:41:01.424 [main] o.c.e.网.单.SelectorServer - 当前事件key: sun.nio.ch.SelectionKeyImpl@3d24753a 
[DEBUG] 16:41:01.424 [main] o.c.e.网.单.SelectorServer - keys.size() = 1 
+--------+-------------------- read -----------------------+----------------+
position: [0], limit: [7]
         +-------------------------------------------------+
0         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
0+--------+-------------------------------------------------+----------------+
0|00000000| 6c 61 64 69 64 6f 6c                            |ladidol         |
0+--------+-------------------------------------------------+----------------+
```



需要主动移除一个key

![image-20221103163554479](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202211031635630.png)

#### 💡 为何要 iter.remove()

> 因为 select 在事件发生后，就会将相关的 key 放入 selectedKeys 集合，但不会在处理完后从 selectedKeys 集合中移除，需要我们自己编码删除。例如
>
> * 第一次触发了 ssckey 上的 accept 事件，没有移除 ssckey 
> * 第二次触发了 sckey 上的 read 事件，但这时 selectedKeys 中还有上次的 ssckey ，在处理时因为没有真正的 serverSocket 连上了，就会导致空指针异常



#### 💡 cancel 的作用

> cancel 会取消注册在 selector 上的 channel，并从 keys 集合中删除 key 后续不会再监听事件

比如在客户端断开连接后，会发起read事件，这时候，需要通过try catch来key.cancel（）；



#### ⚠️  不处理边界的问题

```java
hell
owor
ld�
�好
```

#### 处理消息的边界

![](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202211031706616.png)

* 一种思路是**固定消息长度**，数据包大小一样，服务器按预定长度读取，缺点是浪费带宽

* 另一种思路是**按分隔符拆分**，缺点是效率低

* 第三种思路是TLV 格式，即 Type 类型、Length 长度、Value 数据，类型和长度已知的情况下，就可以方便获取消息大小，分配合适的 buffer，缺点是 buffer 需要提前分配，如果内容过大，则影响 server 吞吐量

  * Http 1.1 是 TLV 格式
  * Http 2.0 是 LTV 格式



  

图示：

![image-20221103170826453](https://figurebed-ladidol.oss-cn-chengdu.aliyuncs.com/img/202211031708721.png)



```java
private static void split(ByteBuffer source) {
    source.flip();
    for (int i = 0; i < source.limit(); i++) {
        // 找到一条完整消息
        if (source.get(i) == '\n') {
            int length = i + 1 - source.position();
            // 把这条完整消息存入新的 ByteBuffer
            ByteBuffer target = ByteBuffer.allocate(length);
            // 从 source 读，向 target 写
            for (int j = 0; j < length; j++) {
                target.put(source.get());
            }
            debugAll(target);
        }
    }
    source.compact(); // 0123456789abcdef  position 16 limit 16
}

public static void main(String[] args) throws IOException {
    // 1. 创建 selector, 管理多个 channel
    Selector selector = Selector.open();
    ServerSocketChannel ssc = ServerSocketChannel.open();
    ssc.configureBlocking(false);
    // 2. 建立 selector 和 channel 的联系（注册）
    // SelectionKey 就是将来事件发生后，通过它可以知道事件和哪个channel的事件
    SelectionKey sscKey = ssc.register(selector, 0, null);
    // key 只关注 accept 事件
    sscKey.interestOps(SelectionKey.OP_ACCEPT);
    log.debug("sscKey:{}", sscKey);
    ssc.bind(new InetSocketAddress(8080));
    while (true) {
        // 3. select 方法, 没有事件发生，线程阻塞，有事件，线程才会恢复运行
        // select 在事件未处理时，它不会阻塞, 事件发生后要么处理，要么取消，不能置之不理
        selector.select();
        // 4. 处理事件, selectedKeys 内部包含了所有发生的事件
        Iterator<SelectionKey> iter = selector.selectedKeys().iterator(); // accept, read
        while (iter.hasNext()) {
            SelectionKey key = iter.next();
            // 处理key 时，要从 selectedKeys 集合中删除，否则下次处理就会有问题
            iter.remove();
            log.debug("key: {}", key);
            // 5. 区分事件类型
            if (key.isAcceptable()) { // 如果是 accept
                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                SocketChannel sc = channel.accept();
                sc.configureBlocking(false);
                ByteBuffer buffer = ByteBuffer.allocate(16); // attachment
                // 将一个 byteBuffer 作为附件关联到 selectionKey 上
                SelectionKey scKey = sc.register(selector, 0, buffer);
                scKey.interestOps(SelectionKey.OP_READ);
                log.debug("{}", sc);
                log.debug("scKey:{}", scKey);
            } else if (key.isReadable()) { // 如果是 read
                try {
                    SocketChannel channel = (SocketChannel) key.channel(); // 拿到触发事件的channel
                    // 获取 selectionKey 上关联的附件
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    int read = channel.read(buffer); // 如果是正常断开，read 的方法的返回值是 -1
                    if(read == -1) {
                        key.cancel();
                    } else {
                        split(buffer);
                        // 需要扩容
                        if (buffer.position() == buffer.limit()) {
                            ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                            buffer.flip();
                            newBuffer.put(buffer); // 0123456789abcdef3333\n
                            key.attach(newBuffer);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    key.cancel();  // 因为客户端断开了,因此需要将 key 取消（从 selector 的 keys 集合中真正删除 key）
                }
            }
        }
    }
}
```

客户端

```java
SocketChannel sc = SocketChannel.open();
sc.connect(new InetSocketAddress("localhost", 8080));
SocketAddress address = sc.getLocalAddress();
// sc.write(Charset.defaultCharset().encode("hello\nworld\n"));
sc.write(Charset.defaultCharset().encode("0123\n456789abcdef"));
sc.write(Charset.defaultCharset().encode("0123456789abcdef3333\n"));
System.in.read();
```



  ```mermaid
  sequenceDiagram 
  participant c1 as 客户端1
  participant s as 服务器
  participant b1 as ByteBuffer1
  participant b2 as ByteBuffer2
  c1 ->> s: 发送 01234567890abcdef3333\r
  s ->> b1: 第一次 read 存入 01234567890abcdef
  s ->> b2: 扩容
  b1 ->> b2: 拷贝 01234567890abcdef
  s ->> b2: 第二次 read 存入 3333\r
  b2 ->> b2: 01234567890abcdef3333\r
  ```



#### ByteBuffer 大小分配

* 每个 channel 都需要记录可能被切分的消息，因为 ByteBuffer 不能被多个 channel 共同使用，因此需要为每个 channel 维护一个独立的 ByteBuffer
* ByteBuffer 不能太大，比如一个 ByteBuffer 1Mb 的话，要支持百万连接就要 1Tb 内存，因此需要设计大小可变的 ByteBuffer
  * 一种思路是首先分配一个较小的 buffer，例如 4k，如果发现数据不够，再分配 8k 的 buffer，将 4k buffer 内容拷贝至 8k buffer，优点是消息连续容易处理，缺点是数据拷贝耗费性能，参考实现 [http://tutorials.jenkov.com/java-performance/resizable-array.html](http://tutorials.jenkov.com/java-performance/resizable-array.html)
  * 另一种思路是用多个数组组成 buffer，一个数组不够，把多出来的内容写入新的数组，与前面的区别是消息存储不连续解析复杂，优点是避免了拷贝引起的性能损耗



**后面Netty会详细处理消息边界。**







### 4.5 处理 write 事件



#### 一次无法写完例子

* 非阻塞模式下，无法保证把 buffer 中所有数据都写入 channel，因此需要追踪 write 方法的返回值（代表实际写入字节数）
* 用 selector 监听所有 channel 的可写事件，每个 channel 都需要一个 key 来跟踪 buffer，但这样又会导致占用内存过多，就有两阶段策略
  * 当消息处理器第一次写入消息时，才将 channel 注册到 selector 上
  * selector 检查 channel 上的可写事件，如果所有的数据写完了，就取消 channel 的注册
  * 如果不取消，会每次可写均会触发 write 事件



```java
public class WriteServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        while(true) {
            selector.select();

            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    SelectionKey sckey = sc.register(selector, SelectionKey.OP_READ);
                    // 1. 向客户端发送内容
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 3000000; i++) {
                        sb.append("a");
                    }
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());
                    int write = sc.write(buffer);
                    // 3. write 表示实际写了多少字节
                    System.out.println("实际写入字节:" + write);
                    // 4. 如果有剩余未读字节，才需要关注写事件
                    if (buffer.hasRemaining()) {
                        // read 1  write 4
                        // 在原有关注事件的基础上，多关注 写事件
                        sckey.interestOps(sckey.interestOps() + SelectionKey.OP_WRITE);
                        // 把 buffer 作为附件加入 sckey
                        sckey.attach(buffer);
                    }
                } else if (key.isWritable()) {
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    int write = sc.write(buffer);
                    System.out.println("实际写入字节:" + write);
                    if (!buffer.hasRemaining()) { // 写完了
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                        key.attach(null);
                    }
                }
            }
        }
    }
}
```

客户端

```java
public class WriteClient {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
        sc.connect(new InetSocketAddress("localhost", 8080));
        int count = 0;
        while (true) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isConnectable()) {
                    System.out.println(sc.finishConnect());
                } else if (key.isReadable()) {
                    ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
                    count += sc.read(buffer);
                    buffer.clear();
                    System.out.println(count);
                }
            }
        }
    }
}
```



#### 💡 write 为何要取消

只要向 channel 发送数据时，socket 缓冲可写，这个事件会频繁触发，因此应当只在 socket 缓冲区写不下时再关注可写事件，数据写完之后再取消关注



2022年11月3日19:54:22，这里有点看不下去了。后面可以再看看。








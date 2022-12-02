package com.dian.sixggroup;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@SpringBootTest
class SixggroupApplicationTests {

    @Test
    public void testSocket() {
        Socket socket = null;
        try {
            // 初始化套接字，设置访问服务的主机和进程端口号，HOST是访问python进程的主机名称，可以是IP地址或者域名，PORT是python进程绑定的端口号
            socket = new Socket("127.0.0.1", 8701);
            // 获取输出流对象
            PrintStream out = new PrintStream(socket.getOutputStream());
            // 发送内容
            String s = "path/from/java/a.jpg";
            System.out.println("java端发送："+ s);
            out.println(s);
            // 告诉服务进程，内容发送完毕，可以开始处理
            // 获取服务进程的输入流
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            System.out.println("python端回复："+br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}

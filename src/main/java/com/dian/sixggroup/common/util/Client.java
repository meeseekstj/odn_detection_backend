package com.dian.sixggroup.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @Author: tanjun
 * @CreateTime: 2022-11-21 22:09
 */
@Component
public class Client {

    private Logger logger = LoggerFactory.getLogger(Client.class);
    @Value("${remote.host}")
    private String host;
    @Value("${remote.port}")
    private int port;

    public String remoteCall(String imgPath) {
        // 访问服务进程的套接字
        Socket socket = null;
        try {
            // 初始化套接字，设置访问服务的主机和进程端口号，HOST是访问python进程的主机名称，可以是IP地址或者域名，PORT是python进程绑定的端口号
            socket = new Socket(host, port);
            // 获取输出流对象
            PrintStream out = new PrintStream(socket.getOutputStream());
            // 发送内容
            out.println(imgPath);
            // 告诉服务进程，内容发送完毕，可以开始处理
            // 获取服务进程的输入流
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }
}

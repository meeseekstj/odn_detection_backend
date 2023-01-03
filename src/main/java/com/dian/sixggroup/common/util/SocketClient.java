package com.dian.sixggroup.common.util;

import com.dian.sixggroup.common.netty.DataBusConstant;
import com.dian.sixggroup.common.netty.NettyClientHandler;
import com.dian.sixggroup.common.netty.NettyClientPool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: tanjun
 * @CreateTime: 2022-11-21 22:09
 */
@Slf4j
public class SocketClient {

    /**
     * netty channel池
     */
    final static NettyClientPool nettyClientPool = NettyClientPool.getInstance();

    public static String remoteCall(String imgPath) {
        // 访问服务进程的套接字
        Socket socket = null;
        String[] addrs = DataBusConstant.ADDRESSES;
        String addr = addrs[(int) (Math.random() % addrs.length)];
        String[] ip_port = addr.split(":");
        try {
            // 初始化套接字，设置访问服务的主机和进程端口号，HOST是访问python进程的主机名称，可以是IP地址或者域名，PORT是python进程绑定的端口号
            socket = new Socket(ip_port[0], Integer.parseInt(ip_port[1]));
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

    public static String remoteCallByNettyChannel(String message) {
        Channel channel = nettyClientPool.getChannel(message.hashCode(), 0);
        log.debug("在链接池池中取到的Channel： " + channel.id());
        UnpooledByteBufAllocator allocator = new UnpooledByteBufAllocator(false);
        ByteBuf buffer = allocator.buffer(20);
        //使用固定分隔符的半包解码器
        String msg = message + DataBusConstant.DELIMITER;
        buffer.writeBytes(msg.getBytes());
        NettyClientHandler tcpClientHandler = channel.pipeline().get(NettyClientHandler.class);
        ChannelId id = channel.id();
        log.info("SEND  MESSAGE AND CHANNEL id [{}]", id);
        String serverMsg = tcpClientHandler.sendMessage(buffer, channel, message);

        return DataBusConstant.RES_PATH + serverMsg;
    }
}

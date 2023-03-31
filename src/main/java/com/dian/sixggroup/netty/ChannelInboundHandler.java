package com.dian.sixggroup.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dian.sixggroup.common.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @Author: tanjun
 * @CreateTime: 2022-12-02 14:37
 */
@Slf4j
public class ChannelInboundHandler extends ChannelInboundHandlerAdapter {

    /**
     * 使用阻塞式LinkedBlockingQueue，对响应结果保存
     * 用于记录通道响应的结果集
     */
    private static final Map<String, LinkedBlockingQueue<Packet>> RESULT_MAP = new ConcurrentHashMap<>();

    volatile static Map<Integer, Set<Channel>> coreChannel = new HashMap();

    public Packet sendMessage(ByteBuf message, Channel ch, String key) {
        LinkedBlockingQueue<Packet> linked = new LinkedBlockingQueue<>(1);
        RESULT_MAP.put(key, linked);
        ch.writeAndFlush(message);

        Packet res = null;
        try {
            //设置获取超时时间或者使用take()--获取不到返回结果一直阻塞
            res = RESULT_MAP.get(key).poll(10, TimeUnit.SECONDS);
            RESULT_MAP.remove(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String message = null;
        if (msg instanceof String) {
            message = msg.toString();
        } else if (msg instanceof ByteBuf) {
            message = ((ByteBuf) msg).toString(Charset.defaultCharset());
        }
        log.info("READ INFO 服务端返回结果:" + message);
        if (message != null) {
            Packet p = JSON.parseObject(message, Packet.class);
            LinkedBlockingQueue<Packet> linked = RESULT_MAP.get(p.getImageUri());
            if (p.getCode() != 1) p.setImageUri(null);
            linked.add(p);
        }
        ReferenceCountUtil.release(msg);

    }

    public byte[] readFileToByteBuffer(String filepath) {
        filepath = DataBusConstant.BASE_PATH + filepath;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            InputStream is = Files.newInputStream(Paths.get(filepath));

            int count = 0;
            byte[] b = new byte[8 * 1024];

            while ((count = is.read(b)) != -1)
                out.write(b, 0, count);

            is.close();

            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        Channel channel = ctx.channel();
        if (evt instanceof IdleStateEvent) {
            //当客户端开始发送心跳检测时。说明没有业务请求过来，释放通道数为设定的 CORE_CONNECTIONS
            if (channel.isActive()) {
                //使用pool的hash值作为Key，维护 CORE_CONNECTIONS个数个通道，多余的关闭
                int poolHash = NettyClientPool.getPoolHash(channel);
                Set<Channel> channels = coreChannel.get(poolHash);
                channels = channels == null ? new HashSet<>(DataBusConstant.CORE_CONNECTIONS) : channels;
                channels.add(channel);
                if (channels.stream().filter(Channel::isActive).count() > DataBusConstant.CORE_CONNECTIONS) {
                    log.info("关闭 CORE_CONNECTIONS 范围之外的通道：{}", channel.id());
                    channels.remove(channel);
                    channel.close();
                } else {
                    //log.info("[客户端心跳监测发送] 通道编号：{}", ctx.channel().id());
                    coreChannel.put(poolHash, channels);
                   /* String heartBeat = DataBusConstant.HEART_BEAT + DataBusConstant.DELIMITER;
                    ByteBuf byteBuf = Unpooled.copiedBuffer(heartBeat.getBytes());
                    channel.writeAndFlush(byteBuf);*/
                }
            }

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}

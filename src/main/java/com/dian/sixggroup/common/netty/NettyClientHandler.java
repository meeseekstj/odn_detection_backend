package com.dian.sixggroup.common.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
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
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    /**
     * 使用阻塞式LinkedBlockingQueue，对响应结果保存
     * 用于记录通道响应的结果集
     */
    private static final Map<Long, LinkedBlockingQueue<String>> RESULT_MAP = new ConcurrentHashMap<>();

    volatile static Map<Integer, Set<Channel>> coreChannel = new HashMap();

    public String sendMessage(ByteBuf message, Channel ch) {
        LinkedBlockingQueue<String> linked = new LinkedBlockingQueue<>(1);
        //获取channel中存储的全局唯一随机值
        Long randomId = ch.attr(AttributeKey.<Long>valueOf(DataBusConstant.RANDOM_KEY)).get();
        RESULT_MAP.put(randomId, linked);
        ch.writeAndFlush(message);
        String res = null;
        try {
            //设置3分钟的获取超时时间或者使用take()--获取不到返回结果一直阻塞
            res = RESULT_MAP.get(randomId).poll(3, TimeUnit.MINUTES);
            RESULT_MAP.remove(randomId);
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
        //获取channel中存储的全局唯一随机值
        Long randomId = ctx.channel().attr(AttributeKey.<Long>valueOf(DataBusConstant.RANDOM_KEY)).get();
        log.info(" READ INFO 服务端返回结果:" + message);
        LinkedBlockingQueue<String> linked = RESULT_MAP.get(randomId);
        linked.add(message);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        boolean active = ctx.channel().isActive();
        log.debug("[此时通道状态] {}", active);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.info("[客户端心跳监测发送] 通道编号：{}", ctx.channel().id());
        Channel channel = ctx.channel();
        if (evt instanceof IdleStateEvent) {
            //当客户端开始发送心跳检测时。说明没有业务请求过来，释放通道数为设定的 CORE_CONNECTIONS
            if (channel.isActive()) {
                //使用pool的hash值作为Key，维护 CORE_CONNECTIONS个数个通道，多余的关闭
                int poolHash = NettyClientPool.getPoolHash(channel);
                Set<Channel> channels = coreChannel.get(poolHash);
                channels = channels == null ? new HashSet<>(DataBusConstant.CORE_CONNECTIONS) : channels;
                channels.add(channel);
                if (channels.stream().filter(x -> x.isActive()).count() > DataBusConstant.CORE_CONNECTIONS) {
                    log.info("关闭 CORE_CONNECTIONS 范围之外的通道：{}", channel.id());
                    channels.remove(channel);
                    channel.close();
                }
                coreChannel.put(poolHash, channels);
            }
            String heartBeat = DataBusConstant.HEART_BEAT + DataBusConstant.DELIMITER;
            ByteBuf byteBuf = Unpooled.copiedBuffer(heartBeat.getBytes());
            channel.writeAndFlush(byteBuf);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}

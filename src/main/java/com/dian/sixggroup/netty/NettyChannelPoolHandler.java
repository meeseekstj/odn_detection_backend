package com.dian.sixggroup.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author: tanjun
 * @CreateTime: 2022-12-02 14:37
 */
@Slf4j
public class NettyChannelPoolHandler implements ChannelPoolHandler {

    static final ByteBuf byteBuf = Unpooled.copiedBuffer(DataBusConstant.DELIMITER.getBytes());

    @Override
    public void channelReleased(Channel ch){
        ch.writeAndFlush(Unpooled.EMPTY_BUFFER);
        log.info("|-->回收Channel. Channel ID:{} ", ch.id());
    }

    @Override
    public void channelAcquired(Channel ch){
        log.info("|-->获取Channel. Channel ID: " + ch.id());
    }

    @Override
    public void channelCreated(Channel ch){

        log.info("|-->创建Channel. Channel ID: " + ch.id()
                +". Channel REAL HASH: " + System.identityHashCode(ch));
        SocketChannel channel = (SocketChannel) ch;
        channel.config().setKeepAlive(true);
        channel.config().setTcpNoDelay(true);
        channel.pipeline()
                //开启Netty自带的心跳处理器，每5秒发送一次心跳
                .addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS))
                .addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE,byteBuf))
                .addLast(new ChannelInboundHandler());
    }


}
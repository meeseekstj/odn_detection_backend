package com.dian.sixggroup.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @Author: tanjun
 * @CreateTime: 2022-12-02 14:28
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class NettyClientPool {

    /**
     * volatile保持线程之间的可见性，连接池的创建是单例，在这里可加可不加
     */
    volatile private static NettyClientPool nettyClientPool;
    /**
     * key为目标主机的InetSocketAddress对象，value为目标主机对应的连接池
     */
    public ChannelPoolMap<InetSocketAddress, FixedChannelPool> poolMap;

    final EventLoopGroup group = new NioEventLoopGroup();
    final Bootstrap strap = new Bootstrap();


    volatile private static Map<InetSocketAddress, FixedChannelPool> pools = new HashMap<>(4);
    volatile private static List<InetSocketAddress> addressList;

    private NettyClientPool() {
    }

    /**
     * 单例
     *
     * @return
     */
    @Bean(initMethod = "build")
    public static NettyClientPool getInstance() {
        if (nettyClientPool == null) {
            synchronized (NettyClientPool.class) {
                if (nettyClientPool == null) {
                    nettyClientPool = new NettyClientPool();
                }
            }
        }
        return nettyClientPool;
    }

    public void build() {
        log.info("NettyClientPool build......");
        strap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);
        poolMap = new AbstractChannelPoolMap<InetSocketAddress, FixedChannelPool>() {
            @Override
            protected FixedChannelPool newPool(InetSocketAddress key) {
                return new FixedChannelPool(strap.remoteAddress(key), new NettyChannelPoolHandler(),
                        ChannelHealthChecker.ACTIVE, null, -1L,
                        DataBusConstant.MAX_CONNECTIONS, Integer.MAX_VALUE,
                        false, DataBusConstant.lastRecentUsed);
            }

        };
        getInetAddresses(DataBusConstant.ADDRESSES);

        for (InetSocketAddress address : addressList) {
            pools.put(address, poolMap.get(address));
        }
        preheat();
    }

    public void preheat() {
        Random random = new Random();
        ArrayList<Channel> channels = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Channel channel = getChannel(random.nextLong(), 0);
            channels.add(channel);
        }
        for (Channel ch : channels) {
            release(ch);
        }
    }

    /**
     * <pre>功能描述:
     * 根据随机数取出随机的server对应pool，从pool中取出channel
     *   pool.acquiredChannelCount(); 对应池中的channel数目
     *   连接池的动态扩容： 指定最大连接数,如果连接池队列中取不到channel，会自动创建channel，默认使用FIFO的获取方式，回收的channel优先被再次get到
     *   SERVER的宕机自动切换: 指定重试次数，get()发生连接异常，则对随机数+1，从下一个池中重新获取,
     *
     *   后期如有必要可优化为：Server注册到注册中心，从注册中心获取连接池对应的address，或者注册到zookeeper中，都需要单独写实现
     *
     * </pre>
     *
     * @param random
     * @return io.netty.channel.Channel
     * @方法名称 getChannel
     */
    public Channel getChannel(long random, int nthRetry) {
        Channel channel = null;
        try {
            //按时间戳取余
            Long poolIndex = random % pools.size();
            InetSocketAddress address = addressList.get(poolIndex.intValue());
            FixedChannelPool pool = pools.get(address);
            Future<Channel> future = pool.acquire();
            channel = future.get();
            AttributeKey<Long> requestID = AttributeKey.valueOf(DataBusConstant.CHANNEL_KEY);
            channel.attr(requestID).set(random);

            //如果是因为服务端挂点，连接失败而获取不到channel，则随机数执行+1操作，从下一个池获取
        } catch (ExecutionException e) {
            log.error(e.getMessage());
            //每个池，尝试获取取2次
            int count = 2;
            if (nthRetry < addressList.size() * count) {
                nthRetry++;
                return getChannel(++random, nthRetry);
            } else {
                log.error("没有可以获取到channel连接的server，server list [{}]", addressList);
                throw new RuntimeException("没有可以获取到channel连接的server");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }


    /**
     * <pre>功能描述:
     *  回收channel进池，需要保证随机值和getChannel获取到的随机值是同一个，才能从同一个pool中释放资源
     * </pre>
     *
     * @param ch
     * @return void
     * @方法名称 release
     */
    public void release(Channel ch) {
        long random = ch.attr(AttributeKey.<Long>valueOf(DataBusConstant.CHANNEL_KEY)).get();
        ch.flush();
        Long poolIndex = random % pools.size();
        pools.get(addressList.get(poolIndex.intValue())).release(ch);
    }

    /**
     * 获取channel池的hash值
     *
     * @param ch
     * @return
     */
    public static int getPoolHash(Channel ch) {
        long random = ch.attr(AttributeKey.<Long>valueOf(DataBusConstant.CHANNEL_KEY)).get();
        Long poolIndex = random % pools.size();
        return System.identityHashCode(pools.get(addressList.get(poolIndex.intValue())));
    }

    /**
     * <pre>功能描述:
     * 获取服务端server列表，每个server对应一个pool
     * </pre>
     *
     * @param addresses
     * @return void
     * @方法名称 getInetAddredd
     */
    public void getInetAddresses(String[] addresses) {
        addressList = new ArrayList<>(4);
        if (addresses.length == 0) {
            throw new RuntimeException("address列表为空");
        }
        for (String address : addresses) {
            String[] split = address.split(":");
            if (split.length == 0) {
                throw new RuntimeException("[" + address + "]不符合IP:PORT格式");
            }
            addressList.add(new InetSocketAddress(split[0], Integer.parseInt(split[1])));
        }
    }
}
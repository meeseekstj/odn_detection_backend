package com.dian.sixggroup.common.netty;


import org.springframework.beans.factory.annotation.Value;

/**
 * @Author: tanjun
 * @CreateTime: 2022-12-02 14:35
 */
public class DataBusConstant {


    public static final String[] ADDRESSES = new String[]{"127.0.0.1:8701"};
    public static final String DELIMITER = "\n";

    public static final String HEART_BEAT = "ping";

    /**
     * 最大连接数
     */
    public static final int MAX_CONNECTIONS = Integer.MAX_VALUE;

    /**
     * 核心链接数，该数目内的通道 在没有业务请求时发送心跳防止失活，超过部分的通道close掉
     */
    public static final int CORE_CONNECTIONS = 10;

    /**
     * 同一个线程使用同一个全局唯一的随机数，保证从同一个池中获取和释放资源，同时使用改随机数作为Key获取返回值
     */
    public static final String CHANNEL_KEY = "requestID";


    public static final String RES_PATH = "/mnt/data01/caw/demo/DIAN/DemoServer/sources/username/resImage/";
    public static final String BASE_PATH = "/mnt/data01/caw/demo/DIAN/DemoServer/sources/username/uploadImage/";

}
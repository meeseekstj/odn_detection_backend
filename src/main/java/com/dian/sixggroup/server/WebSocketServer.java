package com.dian.sixggroup.server;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dian.sixggroup.common.Packet;
import com.dian.sixggroup.util.SocketClient;
import com.dian.sixggroup.util.Upload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.*;


@ServerEndpoint(value = "/api/ws/{id}", encoders = JsonEncoder.class)
@Component
public class WebSocketServer {

    static Logger log = LoggerFactory.getLogger(WebSocketServer.class);


    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    private String userId = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("id") String id) {
        userId = id;
        if (sessionMap.containsKey(id) && sessionMap.get(id).isOpen()) {
            return;
        } else {
            sessionMap.put(id, session);
        }
        log.info("客户端连接,id:{}", id);

    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("id") String id) {
        sessionMap.remove(id);
        log.info("客户端断开连接,id:{}", id);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param messageBytes 客户端发送过来的消息
     */
    @OnMessage(maxMessageSize = 5242880)
    public void onMessage(byte[] messageBytes, Session session) {
        long t1 = System.currentTimeMillis();
        // 读取标志位
        int flag = (int) messageBytes[0];
        // 截取图像字节数组
        byte[] imageBytes = Arrays.copyOfRange(messageBytes, 1, messageBytes.length);
        String imgPath = Upload.uploadFromBytes(imageBytes);

//        long t2 = System.currentTimeMillis();
//        log.info("upload image cost [{}]ms", t2 - t1);
        Packet packet = SocketClient.remoteCallByNettyChannel(imgPath, flag);
        long t3 = System.currentTimeMillis();
        if (packet != null) {
            log.info("remote call cost [{}]ms, return [{}]", t3 - t1, packet);
            session.getAsyncRemote().sendObject(packet);
        } else {
            log.info("remote call cost [{}]ms, return null", t3 - t1);

        }

    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if ("ping".equals(message)) {
            return;
        }
    }


    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.info("客户端错误: id {}", userId);
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(Session session, String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    /**
     * 发送自定义消息
     */
    public static void sendInfo(String message, String id) {
        if (sessionMap.containsKey(id)) {
            try {
                sessionMap.get(id).getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.error("websocket send failed");
            }
        } else {
            log.error("客户端不存在！ session id: {} ,message: {}", id, message);
        }
    }
}
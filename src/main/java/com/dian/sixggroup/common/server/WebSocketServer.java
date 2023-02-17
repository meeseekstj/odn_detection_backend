package com.dian.sixggroup.common.server;


import com.dian.sixggroup.common.util.SocketClient;
import com.dian.sixggroup.common.util.Upload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.imageio.ImageIO;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;


@ServerEndpoint("/api/ws/{id}")
@Component
public class WebSocketServer {

    static Logger log = LoggerFactory.getLogger(WebSocketServer.class);


    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("id") String id) {

        if (sessionMap.containsKey(id) && sessionMap.get(id).isOpen()) {
            return;
        } else {
            sessionMap.put(id, session);
        }
        log.info("客户端连接:" + session.getBasicRemote().toString() + " ,id:" + id);

    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("id") String id) {
        sessionMap.remove(id);
        log.info("客户端断开连接:" + session.getBasicRemote().toString() + " ,id:" + id);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage(maxMessageSize=5242880)
    public void onMessage(byte[] message, Session session) {
        System.out.println("bytes ws");

        long t1 = System.currentTimeMillis();
        String imgPath = Upload.uploadFromBytes(message);
        long t2 = System.currentTimeMillis();
        log.info("upload image cost [{}]ms", t2 - t1);
        String s = SocketClient.remoteCall(imgPath);
        long t3 = System.currentTimeMillis();
        log.info("remote call cost [{}]ms, return [{}]", t3 - t2, s);
        session.getAsyncRemote().sendBinary(readFileToByteBuffer(s));

    }
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("string ws");
    }

    public static ByteBuffer readFileToByteBuffer(String filepath) {
        try {
            InputStream is = Files.newInputStream(Paths.get(filepath));
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int count = 0;
            byte[] b = new byte[8 * 1024];

            while ((count = is.read(b)) != -1)
                out.write(b, 0, count);

            is.close();

            return ByteBuffer.wrap(out.toByteArray());
        } catch (Exception e) {

            return null;
        }
    }


    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.info("客户端错误:" + session.getBasicRemote().toString());
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
    public static void sendInfo(String message, @PathParam("id") String id) {
        if (sessionMap.containsKey(id)) {
            try {
                sessionMap.get(id).getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.error("websocket send failed");
            }
        } else {
            log.error("客户端不存在！" + " session id: " + id + " ,message: " + message);
        }
    }
}
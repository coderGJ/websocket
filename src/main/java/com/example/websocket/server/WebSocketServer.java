package com.example.websocket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/push/websocket/{sid}")
public class WebSocketServer {

    enum OpcodeEnum {
        CONTINUOUS, TEXT, BINARY, PING, PONG, CLOSING
        // more to come
    }

    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    /**
     * sid和session的map
     * key: sid
     * value: session
     */
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * sessionId和sid的map
     * key: sessionId
     * value: sid
     */
    private static final ConcurrentHashMap<String, String> sidMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        sidMap.put(session.getId(), sid);
        sessionMap.put(sid, session);
        log.debug("session id: {}", session.getId());
        log.info("有新窗口开始监听: [{}], 当前在线人数为 {}", sid, sidMap.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        log.debug("[{}] 窗口客户端关闭连接", sidMap.get(session.getId()));
        String sid = sidMap.remove(session.getId());
        sessionMap.remove(sid);
        log.info("有一连接关闭！当前在线人数为: {}", sidMap.size());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * */
    @OnMessage
    public void onMessage(Session session, String message) throws IOException{
        log.info("<==================== 收到来自 [{}] 窗口, 的信息: {}", sidMap.get(session.getId()), message);
        if (OpcodeEnum.PING.toString().equals(message)) {
            sendMessage(session, OpcodeEnum.PONG.toString());
        } else {
            sendMessage(session, "OK");
        }
    }

    /**
     * 发生连接错误
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("[{}] 窗口，发生错误", sidMap.get(session.getId()));
        error.printStackTrace();
    }

    /**
     * 服务端主动推送群消息
     */
    public static void sendGroupMessage(String message) throws IOException{
        log.info("群发消息 ====================>>>> 内容：{}", message);
        for (Session session : sessionMap.values()) {
            sendMessage(session, message);
        }
    }

    /**
     * 指定sid发送消息
     */
    public static void sendMessage(String message, String sid) throws IOException {
        Session session = sessionMap.get(sid);
        if (session != null) {
            sendMessage(session, message);
        } else {
            throw new IOException("session not find!");
        }
    }

    private static void sendMessage(Session session, String message) throws IOException {
        Assert.notNull(session, "session must be exist!");
        log.debug("推送消息到 [{}] 窗口 ====================>>>> 内容：{}", sidMap.get(session.getId()), message);
        session.getBasicRemote().sendText(message);
    }

    public static ConcurrentHashMap<String, Session> getSessionMap() {
        return sessionMap;
    }
}

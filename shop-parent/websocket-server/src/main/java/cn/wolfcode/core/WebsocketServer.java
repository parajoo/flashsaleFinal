package cn.wolfcode.core;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint("/{token}")
@Component
public class WebsocketServer {

    public static Map<String,Session> SESSION_MAP = new ConcurrentHashMap<>();
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token){
        log.info("[WebSocket] new client connected:{}",token);
        //将连接对象session保存起来
        SESSION_MAP.put(token,session);
    }

    @OnClose
    public void onClose(@PathParam("token") String token){
        log.info("[WebSocket] close connection:{}",token);
        SESSION_MAP.remove(token);
    }

    @OnError
    public void onError(Throwable throwable){
        log.info("[WebSocket] abnormal connection:{}",throwable);
    }
}

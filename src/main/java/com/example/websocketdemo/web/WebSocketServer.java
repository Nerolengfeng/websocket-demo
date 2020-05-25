package com.example.websocketdemo.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


//@Component
//@ServerEndpoint("/websocket/{username}")
public class WebSocketServer {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 在线人数
     */
    public static int onlineNumber = 0;
    /**
     * 以用户的姓名为key，WebSocket为对象保存起来
     */
    private static Map<String, WebSocketServer> clients = new ConcurrentHashMap<>();
    /**
     * 会话
     */
    private Session session;
    /**
     * 用户名称
     */
    private String username;
    /**
     * 建立连接
     *
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) {
        onlineNumber++;
        logger.info("现在来连接的客户id："+session.getId()+"用户名："+username);
        this.username = username;
        this.session = session;
        logger.info("有新连接加入！ 当前在线人数" + onlineNumber);
        try {
            //messageType 1代表上线 2代表下线 3代表在线名单 4代表普通消息
            //先给所有人发送通知，说我上线了
            Map<String,Object> map1 = new HashMap<>();
            map1.put("messageType",1);
            map1.put("username",username);
            sendMessageAll(JSON.toJSONString(map1),username);

            //把自己的信息加入到map当中去
            clients.put(username, this);
            //给自己发一条消息：告诉自己现在都有谁在线
            Map<String,Object> map2 = new HashMap<>();
            map2.put("messageType",3);
            //移除掉自己
            Set<String> set = clients.keySet();
            map2.put("onlineUsers",set);
            sendMessageTo(JSON.toJSONString(map2),username);
        }
        catch (IOException e){
            logger.info(username+"上线的时候通知所有人发生了错误");
        }



    }

    
    @OnError
    public void onError(Session session, Throwable error) {
        logger.info("服务端发生了错误"+error.getMessage());
    }
    
    /**
     * 连接关闭
     */
    @OnClose
    public void onClose() {
        onlineNumber--;
        clients.remove(username);
        try {
            //messageType 1代表上线 2代表下线 3代表在线名单  4代表普通消息
            Map<String,Object> map1 = new HashMap<>();
            map1.put("messageType",2);
            map1.put("onlineUsers",clients.keySet());
            map1.put("username",username);
            sendMessageAll(JSON.toJSONString(map1),username);
        }
        catch (IOException e){
            logger.info(username+"下线的时候通知所有人发生了错误");
        }
        logger.info("有连接关闭！ 当前在线人数" + onlineNumber);
    }

    /**
     * 收到客户端的消息
     *
     * @param message 消息
     * @param session 会话
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            logger.info("来自客户端消息：" + message+"客户端的id是："+session.getId());
            JSONObject jsonObject = JSON.parseObject(message);
            String textMessage = jsonObject.getString("message");
            String fromusername = jsonObject.getString("username");
            String tousername = jsonObject.getString("to");
            //如果不是发给所有，那么就发给某一个人
            //messageType 1代表上线 2代表下线 3代表在线名单  4代表普通消息
            Map<String,Object> map1 = new HashMap<>();
            map1.put("messageType",4);
            map1.put("textMessage",textMessage);
            map1.put("fromusername",fromusername);
            if(tousername.equals("All")){
                map1.put("tousername","所有人");
                sendMessageAll(JSON.toJSONString(map1),fromusername);
            }
            else{
                map1.put("tousername",tousername);
                sendMessageTo(JSON.toJSONString(map1),tousername);
            }
        }
        catch (Exception e){
            logger.info("发生了错误了");
        }

    }

    /**
      * @MethodName: sendMessageTo
      * @Description: 给单独人发
      * @Param: [message, toUserName]
      * @Return: void
      * @Author: duanss Nero
      * @Date: 2020/4/7 18:55
      **/
    public void sendMessageTo(String message, String toUserName) throws IOException {
        if(clients.containsKey(toUserName)){
            WebSocketServer webSocketServer = clients.get(toUserName);
            webSocketServer.session.getAsyncRemote().sendText(message);

        }
        
    }

    /** @MethodName: sendMessageAll
      * @Description: 给所有的人发websocket
      * @Param: [message, fromUserName]
      * @Return: void
      * @Author: duanss Nero
      * @Date: 2020/4/7
      **/
    public void sendMessageAll(String message,String fromUserName) throws IOException {
        for (WebSocketServer item : clients.values()) {
            item.session.getAsyncRemote().sendText(message);
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineNumber;
    }

}
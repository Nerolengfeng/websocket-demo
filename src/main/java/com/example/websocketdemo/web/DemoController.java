package com.example.websocketdemo.web;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//@RestController
@Slf4j
public class DemoController {

    @Autowired
    private WebSocketServer webSocketServer;

    @GetMapping("index")
    public ResponseEntity<String> index(){
        return ResponseEntity.ok("请求成功");
    }

    @GetMapping("page/{name}")
    public ModelAndView page(@PathVariable String name){

        Map<String, String> map = new HashMap<>(1);
        map.put("username",name);
        return new ModelAndView("websocket", map);
    }

    @RequestMapping("/push/{toUserId}")
    public ResponseEntity<String> pushToWeb(String message, @PathVariable String toUserId) throws IOException {
        log.info("管理员给{},发送消息:{}",toUserId,message);
        Map<String,Object> map1 = new HashMap<>();
        map1.put("messageType",4);
        map1.put("textMessage",message);
        map1.put("fromusername","admin");
        map1.put("tousername",toUserId);
        webSocketServer.sendMessageTo(JSON.toJSONString(map1),toUserId);
        return ResponseEntity.ok("MSG SEND SUCCESS !");
    }

//    @GetMapping("admin")
//    public ResponseEntity<String> getAllUser(){
//        webSocketServer
//    }
}

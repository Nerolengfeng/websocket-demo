package com.example.websocketdemo;

import com.example.websocketdemo.config.ServerByNetty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebsocketDemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketDemoApplication.class, args);
    }
    
    @Autowired
    private ServerByNetty netty;
    
    @Override
    public void run(String... args) throws Exception {
        netty.startServer();
    }
}

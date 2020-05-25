package com.example.websocketdemo.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.context.annotation.Configuration;

/**
 * @program: websocket-demo
 * @description:
 * @author: 段闪闪 duanss
 * @create: 2020-05-25 16:28
 **/
@Configuration
public class ServerByNetty {
    
    public void startServer() throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {

            serverBootstrap.group(boss, worker)
                    /** 指定所使用的的 NIO 传输的 Channel */
                    .channel(NioServerSocketChannel.class)
                    /** 绑定服务器端口 */
                    .localAddress(9999)
//                    .handler(new LoggingHandler(LogLevel.INFO))
//                    .option(ChannelOption.SO_KEEPALIVE, true)
//                    .option(ChannelOption.SO_BACKLOG, 1024 * 1024 * 10)
                    /** 添加我们自己的业务处理 Handler 到子级的 Channel 的 ChannelPipeline中  */
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            /** 新增HTTP处理器，处理请求升级问题 */
                            socketChannel.pipeline().addLast(new HttpServerCodec());
                            socketChannel.pipeline().addLast(new HttpObjectAggregator(64 * 1024));
                            /** 安装 WebSocket 处理器 */
                            socketChannel.pipeline().addLast(new WebSocketServerProtocolHandler("/hello", null, true, 65535));
                            socketChannel.pipeline().addLast(new NotifyHandler());
                        }
                    });
            /** 异步绑定服务器，阻塞到直到绑定完成 */
            ChannelFuture future = serverBootstrap.bind().sync();
            /** 获取 Channel 的 CloseFuture 阻塞到关闭完成 */
            future.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}

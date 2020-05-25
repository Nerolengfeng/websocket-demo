package com.example.websocketdemo.config;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @program: websocket-demo
 * @description:
 * @author: 段闪闪 duanss
 * @create: 2020-05-25 16:38
 **/
@Configuration
public class NotifyHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    public static ChannelGroup channelGroup;
    
    static {
        channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    //存储ip和channel的容器
    private static ConcurrentMap<String, Channel> channelMap = new ConcurrentHashMap<>();

    /**
     * Handler活跃状态，表示连接成功
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("与客户端连接成功");
        channelGroup.add(ctx.channel());
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        System.out.println("jinru"+msg);
        /** 获取浏览器发送的消息 */
        String text = msg.text();
        System.out.println("接收到消息：" + text);

        ctx.writeAndFlush(new TextWebSocketFrame(text));
        
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                /** 每隔1秒发送一个消息 */
                ctx.writeAndFlush(new TextWebSocketFrame("你有一个新的淘金订单，请尽快处理"));
            }
        }).start();
    }

    /**
     * 未注册状态
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("等待连接");
    }

    /**
     * 非活跃状态，没有连接远程主机的时候。
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端关闭");
        channelGroup.remove(ctx.channel());
    }


    /**
     * 异常处理
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("连接异常："+cause.getMessage());
        ctx.close();
    }
    /**
     * 给指定用户发内容
     * 后续可以掉这个方法推送消息给客户端
     */
    public void  sendMessage(String address){
        Channel channel=channelMap.get(address);
        String message="你好，这是指定消息发送";
        channel.writeAndFlush(new TextWebSocketFrame(message));
    }

    /**
     * 群发消息
     */
    public void  sendMessageAll(){
        String meesage="这是群发信息";
        channelGroup.writeAndFlush(new TextWebSocketFrame(meesage));
    }
}

package com.panda.thread;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ThreadLocalRandom;

@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public static final ChannelHandler INSTANCE = new ServerHandler();

    //channelread0是主线程
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        ByteBuf data = Unpooled.directBuffer();
        //从客户端读一个时间戳
        data.writeBytes(msg);
        //模拟一次业务处理，有可能是数据库操作，也有可能是逻辑处理
        Object result = getResult(data);
        //重新写回给客户端
        ctx.channel().writeAndFlush(result);
    }

    //模拟去数据库拿到一个结果
    protected Object getResult(ByteBuf data) {
        //模拟1000次请求
        int level = ThreadLocalRandom.current().nextInt(1, 1000);
        //模拟每次响应需要的时间，用来作为QPS的参考数据
        int time;
        //1000次请求中，有100次请求响应时间超过1ms
        if (level <= 900) {
            time = 1;
        }
        //1000次请求中，允许有50次请求响应时间超过10ms
        else if (level <=950) {
            time = 10;
        }
        //1000次请求中，允许有10次请求响应时间超过100ms
        else if (level <= 990) {
            time = 100;
        }
        //1000次请求中，允许有1次请求响应时间超过1000ms。即else if(level <= 999)
        else {
            time = 1000;
        }

        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return data;
    }
}

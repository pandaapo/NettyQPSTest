package com.panda.thread;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerThreadPoolHandler extends ServerHandler{
    public static final ChannelHandler INSTANCE = new ServerThreadPoolHandler();
    private static ExecutorService threadPool = Executors.newFixedThreadPool(100);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        final ByteBuf data = Unpooled.directBuffer();
        data.writeBytes(msg);
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                Object result = getResult(data);
                ctx.channel().writeAndFlush(result);
            }
        });
    }
}

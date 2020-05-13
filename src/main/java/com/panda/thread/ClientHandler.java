package com.panda.thread;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public static final ChannelHandler INSTANCE = new ClientHandler();

    private static AtomicLong beginTime = new AtomicLong(0);
    //总响应时间
    private static AtomicLong totalResponseTime = new AtomicLong(0);
    //总请求数
    private static AtomicLong totalRequest = new AtomicLong(0);

    public static final Thread THREAD = new Thread() {
        @Override
        public void run() {
            try {
                while (true) {
                    long duration = System.currentTimeMillis() - beginTime.get();
                    if (duration != 0) {
                        System.out.println("QPS: " + 1000 * totalRequest.get() / duration + "," + "平均响应时间：" + totalResponseTime.get() / totalRequest.get() + "ms.");
                        Thread.sleep(2000);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.executor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                ByteBuf byteBuf = ctx.alloc().ioBuffer();
                //将当前系统时间发送到服务端
                byteBuf.writeLong(System.currentTimeMillis());
                ctx.channel().writeAndFlush(byteBuf);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
        //获取一个响应时间差，本次请求的响应时间
        totalResponseTime.addAndGet(System.currentTimeMillis() - msg.readLong());
        //每次自增
        totalRequest.incrementAndGet();
        if (beginTime.compareAndSet(0, System.currentTimeMillis())) {
            THREAD.start();
        }
    }
}

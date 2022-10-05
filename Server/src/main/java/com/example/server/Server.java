package com.example.server;

import com.example.common.ConfigHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final int PORT = Integer.parseInt(ConfigHandler.handleConfig(ConfigHandler.Name.Server).getProperty("PORT"));
    static Logger logger = Logger.getLogger(Server.class.getName());

    public void run() {
        logger.log(Level.INFO, "Запуск сервера на порту " + PORT);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(1024 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new ServerHandler()
                            );
                        }
                    });
            ChannelFuture future = bootstrap.bind(PORT).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}


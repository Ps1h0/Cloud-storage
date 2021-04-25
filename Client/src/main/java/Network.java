import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;

//TODO дописать метод initChannel (определиться с pipeline'ами)
public class Network {
    private static final String ADDRESS = "localhost";
    private static final int PORT = 8189;
    private SocketChannel channel;

    public Network(){
        new Thread(() ->{
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try{
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                channel = socketChannel;
//                                socketChannel.pipeline().addLast();
                            }
                        });
                ChannelFuture future = bootstrap.connect(ADDRESS, PORT).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e){
                e.printStackTrace();
            }finally {
                workerGroup.shutdownGracefully();
            }
        }).start();
    }

    public void synchronize(String str) throws IOException {
        channel.writeAndFlush(str);
    }
}

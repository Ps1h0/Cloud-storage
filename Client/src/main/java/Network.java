import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.file.Path;

public class Network {
    private static final String ADDRESS = "localhost";
    private static final int PORT = 8189;
    private SocketChannel channel;


    public Network(Controller controller){
        new Thread(() ->{
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try{
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                channel = socketChannel;
                                socketChannel.pipeline().addLast(
                                        new ObjectDecoder(1024 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                                        new ObjectEncoder(),
//                                        new StringDecoder(),
//                                        new StringEncoder(),
//                                        new JSONDecoder(),
//                                        new JSONEncoder(),
                                        new InHandler(controller),
                                        new OutHandler()
                                );
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

    public void synchronize(SynchronizerRequest synchronizerRequest) {
        channel.writeAndFlush(synchronizerRequest);
    }

    public void closeConnection(){
        channel.close();
    }

    public void sendFile(Path path){
        channel.writeAndFlush(path);
    }

    public void deleteFromServer(Path path){
        channel.writeAndFlush(new DeleteRequest(path));
    }


    public void sendFromServer(Path sendPath) {
        channel.writeAndFlush(new SendFromServerRequest(sendPath));
    }
}

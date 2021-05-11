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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

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
                                        new InHandler(controller)
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

    public void sendFile(Path path) throws IOException {
        if (Files.isDirectory(path)){
            List<FileInfo> files = new ArrayList<>();

            Files.walkFileTree(path, new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    files.add(new FileInfo(dir));
                    //channel.writeAndFlush(new FileInfo(dir));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    files.add(new FileInfo(file));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
            channel.writeAndFlush(new FilesListResponse(files));
        }
        if (Files.isRegularFile(path)){
            FileInfo fileInfo = new FileInfo(path);
            channel.writeAndFlush(fileInfo);
        }
    }

    public void deleteFromServer(Path path){
        channel.writeAndFlush(new DeleteRequest(path.toString()));
    }


    public void getFromServer(Path sendPath) {
        SendFromServerRequest sendFromServerRequest = new SendFromServerRequest(sendPath.toString());
        channel.writeAndFlush(sendFromServerRequest);
    }

    public void closeConnection(){
        channel.close();
    }
}

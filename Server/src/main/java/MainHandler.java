import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Клиент подключился");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Converter converter = new Converter();
        FileInfo fileInfo = converter.toJavaObject(String.valueOf(msg));
        Path path = Paths.get("./Server/Server Storage/" + fileInfo.getFilename());
        Files.createFile(path);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

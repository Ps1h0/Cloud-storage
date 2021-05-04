import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.nio.file.Path;

public class OutHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Path){
            System.out.println("Передача файла с клиента на сервер");
            System.out.println("Клиентский OUT " + msg);
            FileInfo fileInfo = new FileInfo((Path) msg);
            ctx.writeAndFlush(fileInfo);
        }
        if (msg instanceof FilesListRequest){
            System.out.println("Начинается отправка списка файлов");
            System.out.println(msg);
            ctx.writeAndFlush(msg);
        }
    }

}

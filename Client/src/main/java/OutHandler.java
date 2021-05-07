import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.nio.file.Path;

public class OutHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof Path){
            System.out.println("Передача файла с клиента на сервер");
            FileInfo fileInfo = new FileInfo((Path) msg);
            ctx.writeAndFlush(fileInfo);
        }
        if (msg instanceof FilesListRequest){
            System.out.println("Отправка списка файлов");
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof SynchronizerRequest){
            System.out.println("Запрос на синхронизацию файлов");
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof DeleteRequest){
            System.out.println("Запрос на удаление файла с сервера");
            System.out.println(ctx);
            System.out.println(msg);
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof SendFromServerRequest){
            System.out.println("Запрос на получение файла с сервера");
            System.out.println(((SendFromServerRequest) msg).getPath());
            ctx.writeAndFlush(msg);
        }
    }

}

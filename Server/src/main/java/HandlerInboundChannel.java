import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

//Серверный обработчик сообщений
public class HandlerInboundChannel extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Клиент подключился");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Серверный IN " + msg);
        if (msg instanceof FilesListRequest){
            File dir = new File("./Server/Server storage");
            List<FileInfo> files = new ArrayList<>();
            Files.walkFileTree(dir.toPath(), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    files.add(new FileInfo(file));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
            FilesListResponse filesListResponse = new FilesListResponse(files);
            ctx.writeAndFlush(filesListResponse);
        }
        if (msg instanceof FileInfo){
            FileInfo fileInfo = (FileInfo) msg;
            Path path = Paths.get("./Server/Server Storage/" + fileInfo.getFilename());
            if (!(fileInfo.getType() == FileInfo.FileType.DIRECTORY)){
                Files.createFile(path);
                File file = new File(path.toString());
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(fileInfo.getFileContent());
                fo.close();
                System.out.println("файл принят");
            }else{
                System.out.println("Передача папок пока не реализована");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

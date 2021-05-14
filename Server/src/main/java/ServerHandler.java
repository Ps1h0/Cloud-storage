import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

//Серверный обработчик входящих сообщений
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final String DIR = "./Server storage/";

    //При подключении отправить клиенту список файлов на серверном хранилище для вывода
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws IOException {
        ctx.writeAndFlush(serverFilesTable(DIR));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileInfo){
            FileInfo fileInfo = (FileInfo) msg;
            acceptFiles(ctx, fileInfo, DIR);
        }

        if (msg instanceof SynchronizerRequest){
            SynchronizerRequest synchronizerRequest = (SynchronizerRequest) msg;
            for (int i = 0; i < synchronizerRequest.getFiles().size(); i++){
                acceptFiles(ctx, synchronizerRequest.getFiles().get(i), DIR);
            }
            SynchronizerResponse synchronizerResponse = new SynchronizerResponse(serverFilesTable(DIR));
            ctx.writeAndFlush(synchronizerResponse);
        }

        if (msg instanceof DeleteRequest){
            DeleteRequest deleteRequest = (DeleteRequest) msg;
            Path path = Path.of(DIR + deleteRequest.getDelPath());
            Files.delete(path);
            ctx.writeAndFlush(serverFilesTable(DIR));
        }

        if (msg instanceof SendFromServerRequest){
            SendFromServerRequest sendFromServerRequest = (SendFromServerRequest) msg;
            Path path = Path.of(DIR + sendFromServerRequest.getPath());
            FileInfo fileInfo = new FileInfo(path);
            ctx.writeAndFlush(fileInfo);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    //Формирует список файлов, хранящихся на сервере
    public FilesListResponse serverFilesTable(String dir) throws IOException {
        File file = new File(dir);
        List<FileInfo> files = new ArrayList<>();

        Files.walkFileTree(file.toPath(), new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
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
        return new FilesListResponse(files);
    }

    //Принимает файлы с клиента
    public void acceptFiles(ChannelHandlerContext ctx, FileInfo fileInfo, String dir) throws IOException {
        Path path = Paths.get(dir).resolve(fileInfo.getFilename());

        if (fileInfo.getType() == FileInfo.FileType.FILE){
            if (Files.exists(path)){
                if (!fileInfo.equals(new FileInfo(path))) {
                    File file = new File(path.toString());
                    FileOutputStream fo = new FileOutputStream(file);
                    fo.write(fileInfo.getFileContent());
                    fo.close();
                    ctx.writeAndFlush(serverFilesTable(DIR));
                }
            }else{
                Files.createFile(path);
                File file = new File(path.toString());
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(fileInfo.getFileContent());
                fo.close();
                ctx.writeAndFlush(serverFilesTable(DIR));
            }
        }
    }
}

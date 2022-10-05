package com.example.server;

import com.example.common.ConfigHandler;
import com.example.common.FileInfo;
import com.example.common.requests.DeleteRequest;
import com.example.common.requests.SendFromServerRequest;
import com.example.common.requests.SynchronizerRequest;
import com.example.common.responses.FilesListResponse;
import com.example.common.responses.SynchronizerResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Серверный обработчик входящих сообщений
 */
@Getter
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final String SERVER_DIRECTORY = ConfigHandler.handleConfig().getProperty("SERVER_DIRECTORY");

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(createServerFilesTable(SERVER_DIRECTORY));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FileInfo) {
            FileInfo fileInfo = (FileInfo) msg;
            acceptFiles(ctx, fileInfo, SERVER_DIRECTORY);
        }

        if (msg instanceof SynchronizerRequest) {
            SynchronizerRequest synchronizerRequest = (SynchronizerRequest) msg;
            for (int i = 0; i < synchronizerRequest.getFiles().size(); i++) {
                acceptFiles(ctx, synchronizerRequest.getFiles().get(i), SERVER_DIRECTORY);
            }
            SynchronizerResponse synchronizerResponse = new SynchronizerResponse(createServerFilesTable(SERVER_DIRECTORY));
            ctx.writeAndFlush(synchronizerResponse);
        }

        if (msg instanceof DeleteRequest) {
            DeleteRequest deleteRequest = (DeleteRequest) msg;
            Path path = Path.of(SERVER_DIRECTORY + deleteRequest.getDelPath());
            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ctx.writeAndFlush(createServerFilesTable(SERVER_DIRECTORY));
        }

        if (msg instanceof SendFromServerRequest) {
            SendFromServerRequest sendFromServerRequest = (SendFromServerRequest) msg;
            Path path = Path.of(SERVER_DIRECTORY + sendFromServerRequest.getPath());
            FileInfo fileInfo = new FileInfo(path);
            ctx.writeAndFlush(fileInfo);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * Метод для формирования списка файлов, хранящихся на сервере
     *
     * @param dir директория
     * @return список файлов
     */
    private FilesListResponse createServerFilesTable(String dir) {
        List<FileInfo> files = new ArrayList<>();

        try {
            Files.walkFileTree(Path.of(dir), new FileVisitor<>() {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FilesListResponse(files);
    }

    /**
     * Принимает файлы с клиента
     *
     * @param ctx      контекст
     * @param fileInfo информация о файлах
     * @param dir      директория
     */
    private void acceptFiles(ChannelHandlerContext ctx, FileInfo fileInfo, String dir) {
        Path path = Paths.get(dir).resolve(fileInfo.getFilename());
        File file;
        if (fileInfo.getType() == FileInfo.FileType.FILE) {
            if (Files.exists(path)) {
                if (!fileInfo.equals(new FileInfo(path))) {
                    file = new File(path.toString());
                    write(fileInfo, file);
                    ctx.writeAndFlush(createServerFilesTable(SERVER_DIRECTORY));
                }
            } else {
                try {
                    file = new File(Files.createFile(path).toString());
                    write(fileInfo, file);
                    ctx.writeAndFlush(createServerFilesTable(SERVER_DIRECTORY));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void write(FileInfo fileInfo, File file) {
        try (FileOutputStream fo = new FileOutputStream(file)) {
            fo.write(fileInfo.getFileContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


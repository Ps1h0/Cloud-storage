package com.example.client.handlers;

import com.example.client.Controller;
import com.example.common.entities.FileInfo;
import com.example.common.responses.FilesListResponse;
import com.example.common.responses.SynchronizerResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Обработчик входящих сообщений от сервера
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private final Controller controller;
    private final Path defaultPath;

    public ClientHandler(Controller controller) {
        this.controller = controller;
        defaultPath = controller.getDefaultPathToStorage();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FilesListResponse) {
            FilesListResponse filesListResponse = (FilesListResponse) msg;
            controller.fillTable(filesListResponse);
        }
        if (msg instanceof FileInfo) {
            FileInfo fileInfo = (FileInfo) msg;
            acceptFiles(fileInfo);
        }
        if (msg instanceof SynchronizerResponse) {
            SynchronizerResponse synchronizerResponse = (SynchronizerResponse) msg;
            for (int i = 0; i < synchronizerResponse.getFiles().getFiles().size(); i++) {
                acceptFiles(synchronizerResponse.getFiles().getFiles().get(i));
            }
            controller.fillTable(synchronizerResponse.getFiles());
            controller.updateTable(defaultPath);
        }
    }

    private void acceptFiles(FileInfo fileInfo) {
        Path path = defaultPath.resolve(fileInfo.getFilename());
        File file;
        if (fileInfo.getType() == FileInfo.FileType.FILE) {
            if (Files.exists(path)) {
                if (!fileInfo.equals(new FileInfo(path))) {
                    file = new File(path.toString());
                    write(fileInfo, file);
                    controller.updateTable(defaultPath);
                }
            } else {
                try {
                    file = new File(Files.createFile(path).toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                write(fileInfo, file);
                controller.updateTable(defaultPath);
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


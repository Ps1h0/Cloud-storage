import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//Обработчик входящих сообщений от сервера
public class InHandler extends ChannelInboundHandlerAdapter {

    Controller controller;

    public InHandler(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FilesListResponse){
            FilesListResponse filesListResponse = (FilesListResponse) msg;
            controller.fillServerTable(filesListResponse);
        }
        if (msg instanceof FileInfo){
            FileInfo fileInfo = (FileInfo) msg;
            acceptFiles(fileInfo);
        }
        if (msg instanceof SynchronizerResponse){
            SynchronizerResponse synchronizerResponse = (SynchronizerResponse) msg;
            for (int i = 0; i < synchronizerResponse.getFiles().getFiles().size(); i++){
                acceptFiles(synchronizerResponse.getFiles().getFiles().get(i));
            }
            controller.fillClientTable(synchronizerResponse.getFiles());
        }
    }

    private void acceptFiles(FileInfo fileInfo) throws IOException {
        Path path = Paths.get("./Client/Client Storage/" + fileInfo.getFilename());

        if (fileInfo.getType() == FileInfo.FileType.FILE){
            if (Files.exists(path)){
                if (!fileInfo.equals(new FileInfo(path))){
                    File file = new File(path.toString());
                    FileOutputStream fo = new FileOutputStream(file);
                    fo.write(fileInfo.getFileContent());
                    fo.close();
                    controller.updateTable(Path.of("./Client/Client Storage/"));
                }
            }else{
                Files.createFile(path);
                File file = new File(path.toString());
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(fileInfo.getFileContent());
                fo.close();
                controller.updateTable(Path.of("./Client/Client Storage/"));
            }
        }
    }
}

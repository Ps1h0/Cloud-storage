import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.fxml.FXMLLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InHandler extends ChannelInboundHandlerAdapter {

    FXMLLoader loader = new FXMLLoader();
    Controller controller;

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("Клиентский IN " + msg);
//        System.out.println("Получение файлов от сервера");
//        FileInfo fileInfo = (FileInfo) msg;
//        Path path = Paths.get("./Client/Client Storage/" + fileInfo.getFilename());
//        if (fileInfo.getType() == FileInfo.FileType.DIRECTORY){
//            System.out.println("Передача папок пока не реализована");
//        }else{
//            Files.createFile(path);
//            File file = new File(path.toString());
//            FileOutputStream fo = new FileOutputStream(file);
//            fo.write(fileInfo.getFileContent());
//            fo.close();
//        }
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("Клиентский IN " + msg);
        System.out.println(msg);
        if (msg instanceof FilesListResponse){
            loader.setController(controller);
            controller = loader.getController();
            FilesListResponse filesListResponse = (FilesListResponse) msg;
            controller.zapolnitTable(filesListResponse);
        }
        if (msg instanceof FileInfo){
            FileInfo fileInfo = (FileInfo) msg;
            Path path = Paths.get("./Client/Client Storage/" + fileInfo.getFilename());
            if (fileInfo.getType() == FileInfo.FileType.DIRECTORY){
                System.out.println("Передача папок пока не реализована");
            }else{
                Files.createFile(path);
                File file = new File(path.toString());
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(fileInfo.getFileContent());
                fo.close();
            }
        }
    }

}

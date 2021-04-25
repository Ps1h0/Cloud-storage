import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

//TODO добавить кнопки по функционалу (удаление файла из директории, удаление файла с сервера)
public class Controller implements Initializable {

    public ListView<String> clientStorage;
    public ListView<String> ServerStorage;
    public TextField test;
    private Network network;

    public void quit(ActionEvent actionEvent) {
        Platform.exit();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        network = new Network();
    }


    public void synchronize(ActionEvent actionEvent) throws IOException {
        network.synchronize(test.getText());
        test.clear();
        //TODO реализовать синхронизацию
    }
}

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    private final Path DEFAULT_PATH_TO_STORAGE = Paths.get("./Client/Client Storage");
    public TableView<FileInfo> filesTable;
    public TableView<FileInfo> serverTable;
    public ComboBox<String> disksBox;
    public TextField pathField;
    public Network network;


    public TableView<FileInfo> getServerTable() {
        return serverTable;
    }

    public void setServerTable(TableView<FileInfo> serverTable) {
        this.serverTable = serverTable;
    }

    //Инициализация графического интерфейса
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //Получение соединения
        network = new Network();

        //Заполнение колонки "тип файла" в таблице
        TableColumn<FileInfo, String> clientFileTypeColumn = new TableColumn<>();
        clientFileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        clientFileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfo, String> serverFileTypeColumn = new TableColumn<>();
        serverFileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        serverFileTypeColumn.setPrefWidth(24);


        //Заполнение колонки "имя" в таблице
        TableColumn<FileInfo, String> clientFileNameColumn = new TableColumn<>("Имя");
        clientFileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        clientFileNameColumn.setPrefWidth(150);

        TableColumn<FileInfo, String> serverFileNameColumn = new TableColumn<>("Имя");
        serverFileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        serverFileNameColumn.setPrefWidth(150);


        //Заполнение колонки "размер" в таблице
        TableColumn<FileInfo, Long> clientFileSizeColumn = new TableColumn<>("Размер");
        clientFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        clientFileSizeColumn.setPrefWidth(50);

        TableColumn<FileInfo, Long> serverFileSizeColumn = new TableColumn<>("Размер");
        serverFileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        serverFileSizeColumn.setPrefWidth(50);

        //Добавление элементов в таблицу
        filesTable.getColumns().addAll(clientFileTypeColumn, clientFileNameColumn, clientFileSizeColumn);
        serverTable.getColumns().addAll(serverFileTypeColumn, serverFileNameColumn, serverFileSizeColumn);

        //Сортировка по типу (сначала директории, затем файлы)
        filesTable.getSortOrder().add(clientFileTypeColumn);
        filesTable.getSortOrder().add(serverFileTypeColumn);

        clientFileSizeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d Б", item);
                    if (item == -1L) text = "[DIR]";
                    setText(text);
                }
            }
        });

        //Получить названия всех логических дисков и добавить их в ComboBox
        disksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()){
            disksBox.getItems().add(p.toString());
        }
        disksBox.getSelectionModel().select(0);

        //Переход на уровень вниз в древе при нажатии 2 лкм
        filesTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2){
                Path path = Paths.get(getCurrentPath()).resolve(getSelectedFilename());
                if (Files.isDirectory(path)){
                    updateTable(path);
                }
            }
        });


        updateTable(DEFAULT_PATH_TO_STORAGE);

    }

    //Обновление списка файлов и директорий после каких-либо манипуляций с файлами
    public void updateTable(Path path){
        try {
            pathField.setText(path.normalize().toAbsolutePath().toString());
            filesTable.getItems().clear();
            filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            filesTable.sort();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void zapolnitTable(FilesListResponse filesListResponse){
        serverTable.getItems().clear();
        serverTable.getItems().addAll(filesListResponse.getFiles());
        serverTable.sort();
    }

    //TODO реализовать синхронизацию
    public void synchronize() throws IOException {
//        FilesListResponse filesListResponse = new FilesListResponse();
//        List<FileInfo> list = new ArrayList<>(filesTable.getItems());
        //network.synchronize();
        network.getServerFiles();

    }

    //TODO не работает
    public void quit() {
        Platform.exit();
    }

    //Операция удаления файла/директории из папки на клиенте
    public void deleteFile() {
        if(filesTable.getSelectionModel().getSelectedItem() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Файл не выбран", ButtonType.OK);
            alert.showAndWait();
        }else{
            Path delPath = Paths.get(getCurrentPath(), getSelectedFilename());
            try {
                Files.delete(delPath);
                updateTable(Paths.get(getCurrentPath()));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось удалить выбранный файл", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    //Переход на один уровень вверх в древе
    public void pathUp() {
        Path upperPath = Paths.get(getCurrentPath()).getParent();
        if(upperPath != null){
            updateTable(upperPath);
        }
    }

    //Выбрать логический диск
    public void selectDisk(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateTable(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public void sendToServer() {
        if(filesTable.getSelectionModel().getSelectedItem() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Файл не выбран", ButtonType.OK);
            alert.showAndWait();
        }else{
            Path path = Paths.get(getCurrentPath() + "/" + getSelectedFilename());
            network.sendFile(path);
        }
    }

    public String getSelectedFilename(){
        return filesTable.getSelectionModel().getSelectedItem().getFilename();
    }

    public String getCurrentPath(){
        return pathField.getText();
    }

}

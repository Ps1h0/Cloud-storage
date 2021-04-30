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
    public TableView<FileInfo> serverStorage; //TODO привязать к серверному хранилищу
    public ComboBox<String> disksBox;
    public TextField pathField;
    public Network network;
    public TextField testField;

    //Инициализация графического интерфейса
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Получение соединения
        network = new Network();

        //Заполнение колонки "тип файла" в таблице
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        //Заполнение колонки "имя" в таблице
        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Имя");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumn.setPrefWidth(150);

        //Заполнение колонки "размер" в таблице
        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(50);

        //Добавление элементов в таблицу
        filesTable.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn);

        //Сортировка по типу (сначала директории, затем файлы)
        filesTable.getSortOrder().add(fileTypeColumn);

        fileSizeColumn.setCellFactory(column -> new TableCell<>() {
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

    //TODO реализовать синхронизацию
    public void synchronize(ActionEvent actionEvent) throws IOException {

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

    public void sendToServer(ActionEvent actionEvent) throws IOException {
        Path path = Paths.get("./Client/Client Storage/" + getSelectedFilename());
        FileInfo fileInfo = new FileInfo(path);
        Converter converter = new Converter();
        network.sendMessage(converter.toJSON(fileInfo));
    }

    public String getSelectedFilename(){
        return filesTable.getSelectionModel().getSelectedItem().getFilename();
    }

    public String getCurrentPath(){
        return pathField.getText();
    }
}

package com.example.client;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import com.example.common.FileInfo;
import com.example.common.FilesListResponse;
import com.example.common.SynchronizerRequest;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    //Работает для запуска jar архива, для запуска проекта из среды использовать путь "./Client storage/"
    public final Path DEFAULT_PATH_TO_STORAGE = Paths.get("../Client storage/");
    public TableView<FileInfo> filesTable;
    public TableView<FileInfo> serverTable;
    public ComboBox<String> disksBox;
    public TextField pathField;
    public Network network;

    //Инициализация графического интерфейса
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //Получение соединения
        network = new Network(this);

        createTable(filesTable);
        createTable(serverTable);

        //Получить названия всех логических дисков и добавить их в ComboBox
        disksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()){
            disksBox.getItems().add(p.toString());
        }
        disksBox.getSelectionModel().select(String.valueOf(DEFAULT_PATH_TO_STORAGE.toAbsolutePath().getRoot()));

        //Переход на уровень вниз в древе при нажатии 2 лкм
        filesTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2){
                Path path = Paths.get(getCurrentPath()).resolve(getSelectedFilename(filesTable));
                if (Files.isDirectory(path)){
                    updateTable(path);
                }
            }
        });

        updateTable(DEFAULT_PATH_TO_STORAGE);
    }

    //Создание таблиц в окне
    private void createTable(TableView<FileInfo> tableView){
        //Создание и заполнение колонки "тип файла" в таблице
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(30);

        //Создание и заполнение колонки "имя" в таблице
        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumn.setPrefWidth(180);

        //Создание и заполнение колонки "размер" в таблице
        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Size");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setPrefWidth(100);

        //Добавление элементов в таблицу
        tableView.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn);
        tableView.getSortOrder().add(fileTypeColumn);

        //Отображение размера файлов
        fileSizeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d B", item);
                    if (item == -1L) text = "[DIR]";
                    setText(text);
                }
            }
        });
    }

    //Обновление списка файлов и директорий на клиенте после каких-либо манипуляций с файлами
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

    //Заполнение таблицы данными о файлах на серверном хранилище
    public void fillServerTable(FilesListResponse filesListResponse){
        serverTable.getItems().clear();
        serverTable.getItems().addAll(filesListResponse.getFiles());
        serverTable.sort();
    }

    //Заполнение таблицы данными о файлах на клиенте
    public void fillClientTable(FilesListResponse filesListResponse){
        filesTable.getItems().clear();
        filesTable.getItems().addAll(filesListResponse.getFiles());
        filesTable.sort();
        updateTable(DEFAULT_PATH_TO_STORAGE);
    }

    //Синхронизация файлов клиента и сервера
    public void synchronize() {
        List<FileInfo> files = new ArrayList<>(filesTable.getItems());
        network.synchronize(new SynchronizerRequest(files));
    }

    public void quit() {
        network.closeConnection();
        Platform.exit();
    }

    //Операция удаления файла/директории из папки на клиенте
    public void deleteFileFromClient() {
        if(filesTable.getSelectionModel().getSelectedItem() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "No file selected", ButtonType.OK);
            alert.showAndWait();
        }else{
            Path delPath = Paths.get(getCurrentPath(), getSelectedFilename(filesTable));
            try {
                Files.delete(delPath);
                updateTable(Paths.get(getCurrentPath()));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось удалить выбранный файл", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    public void deleteFileFromServer() {
        if(serverTable.getSelectionModel().getSelectedItem() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "No file selected", ButtonType.OK);
            alert.showAndWait();
        }else{
            if (serverTable.getSelectionModel().getSelectedItem().getType() == FileInfo.FileType.DIRECTORY){
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Directory transfer will be available in the next versions :)", ButtonType.OK);
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }
            Path delPath = Paths.get(getSelectedFilename(serverTable));
            network.deleteFromServer(delPath);
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

    //Отправить файл на сервер
    public void sendToServer() throws IOException {
        if(filesTable.getSelectionModel().getSelectedItem() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "No file selected", ButtonType.OK);
            alert.showAndWait();
        }else{
            if (filesTable.getSelectionModel().getSelectedItem().getType() == FileInfo.FileType.DIRECTORY){
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Directory transfer will be available in the next versions :)", ButtonType.OK);
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }
            Path path = Paths.get(getCurrentPath() + "/" + getSelectedFilename(filesTable));
            network.sendFile(path);
        }
    }

    private String getSelectedFilename(TableView<FileInfo> tableView){
        return tableView.getSelectionModel().getSelectedItem().getFilename();
    }

    private String getCurrentPath(){
        return pathField.getText();
    }

    public void getFileFromServer() {
        if (serverTable.getSelectionModel().getSelectedItem() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "No file selected", ButtonType.OK);
            alert.showAndWait();
        }else{
            Path sendPath = Paths.get(getSelectedFilename(serverTable));
            network.getFromServer(sendPath);
        }
    }
}


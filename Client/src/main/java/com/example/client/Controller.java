package com.example.client;

import com.example.common.handlers.ConfigHandler;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import com.example.common.entities.FileInfo;
import com.example.common.responses.FilesListResponse;
import com.example.common.requests.SynchronizerRequest;
import lombok.Getter;

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

    @Getter
    private final Path defaultPathToStorage = Path.of(ConfigHandler.handleConfig(ConfigHandler.Name.Client).getProperty("CLIENT_DIRECTORY"));

    public TableView<FileInfo> filesTable;
    public TableView<FileInfo> serverTable;
    public ComboBox<String> disksBox;
    @Getter
    public TextField pathField;

    private Network network;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        network = new Network(this);

        createTable(filesTable);
        createTable(serverTable);

        //Получить названия всех логических дисков и добавить их в ComboBox
        disksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            disksBox.getItems().add(p.toString());
        }
        disksBox.getSelectionModel().select(String.valueOf(defaultPathToStorage.toAbsolutePath().getRoot()));

        //Переход на уровень вниз в древе при нажатии 2 лкм
        filesTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                Path path = Paths.get(pathField.getText()).resolve(getSelectedFilename(filesTable));
                if (Files.isDirectory(path)) {
                    updateTable(path);
                }
            }
        });

        updateTable(defaultPathToStorage);
    }

    /**
     * Метод для создания таблиц в окне
     *
     * @param tableView отображения таблицы
     */
    private void createTable(TableView<FileInfo> tableView) {
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

    /**
     * Обновление списка файлов и директорий на клиенте после каких-либо манипуляций с файлами
     *
     * @param path путь к директории
     */
    public void updateTable(Path path) {
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

    /**
     * Заполнение таблицы данными о файлах
     *
     * @param files ответ со списком файлов
     */
    public void fillTable(FilesListResponse files) {
        filesTable.getItems().clear();
        filesTable.getItems().addAll(files.getFiles());
        filesTable.sort();
    }

    /**
     * Синхронизация файлов клиента и сервера
     */
    public void synchronize() {
        List<FileInfo> files = new ArrayList<>(filesTable.getItems());
        network.synchronize(new SynchronizerRequest(files));
    }

    public void quit() {
        network.closeConnection();
        Platform.exit();
    }

    /**
     * Операция удаления файла/директории из папки на клиенте
     */
    public void deleteFileFromClient() {
        if (filesTable.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No file selected", ButtonType.OK);
            alert.showAndWait();
        } else {
            Path delPath = Paths.get(pathField.getText(), getSelectedFilename(filesTable));
            try {
                Files.delete(delPath);
                updateTable(Paths.get(pathField.getText()));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось удалить выбранный файл", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    public void deleteFileFromServer() {
        if (serverTable.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No file selected", ButtonType.OK);
            alert.showAndWait();
        } else {
            if (serverTable.getSelectionModel().getSelectedItem().getType() == FileInfo.FileType.DIRECTORY) {
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
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateTable(upperPath);
        }
    }

    //Выбрать логический диск
    public void selectDisk(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateTable(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    /**
     * Отправка файла на сервер
     */
    public void sendToServer() {
        if (filesTable.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No file selected", ButtonType.OK);
            alert.showAndWait();
        } else {
            if (filesTable.getSelectionModel().getSelectedItem().getType() == FileInfo.FileType.DIRECTORY) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Directory transfer will be available in the next versions :)", ButtonType.OK);
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }
            Path path = Paths.get(pathField.getText() + "/" + getSelectedFilename(filesTable));
            try {
                network.sendFile(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getSelectedFilename(TableView<FileInfo> tableView) {
        return tableView.getSelectionModel().getSelectedItem().getFilename();
    }

    public void getFileFromServer() {
        if (serverTable.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No file selected", ButtonType.OK);
            alert.showAndWait();
        } else {
            Path sendPath = Paths.get(getSelectedFilename(serverTable));
            network.getFromServer(sendPath);
        }
    }
}


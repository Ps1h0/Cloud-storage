import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

//Класс с информацией о файле
public class FileInfo extends AbstractMessage {
    //Перечисление типов файлов (файл или директория)
    public enum FileType{
        FILE("File"), DIRECTORY("Dir");

        private final String name;

        FileType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("type")
    private FileType type;

    @JsonProperty("size")
    private long size;

    @JsonProperty("fileContent")
    private byte[] fileContent;


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public FileInfo() {
    }

    public FileInfo(Path path){
        try {
            this.filename = path.getFileName().toString();
            this.size = Files.size(path);
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (this.type == FileType.DIRECTORY) {
                this.size = -1L;
            } else this.fileContent = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Некорректный файл");
        }
    }
}

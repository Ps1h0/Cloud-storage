import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

//Класс для заполнения таблицы списка файлов на клиенте
public class FileInfo {
    //Перечисление типов файлов (файл или директория)
    public enum FileType{
        FILE("File"), DIRECTORY("Dir");

        private String name;

        FileType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private String filename;
    private FileType type;
    private long size;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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

    public FileInfo(Path path){
        try {
            this.filename = path.getFileName().toString();
            this.size = Files.size(path);
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (this.type == FileType.DIRECTORY) this.size = -1L;
        } catch (IOException e) {
            throw new RuntimeException("Некорректный файл");
        }
    }
}

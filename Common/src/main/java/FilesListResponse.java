import java.util.List;

//Класс ответ на запрос списка файлов. Хранит в себе список с информацией о файлах
public class FilesListResponse extends AbstractMessage{

    private List<FileInfo> files;

    public FilesListResponse() {
    }

    public FilesListResponse(List<FileInfo> files) {
        this.files = files;
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }
}

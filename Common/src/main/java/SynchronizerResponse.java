import java.util.List;

//Ответ сервера на запрос синхронизации
public class SynchronizerResponse extends AbstractMessage{

    private FilesListResponse files;

    public SynchronizerResponse() {
    }

    public SynchronizerResponse(FilesListResponse files) {
        this.files = files;
    }

    public FilesListResponse getFiles() {
        return files;
    }

    public void setFiles(FilesListResponse files) {
        this.files = files;
    }
}

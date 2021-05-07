import java.util.List;

//Запрос серверу на синхронизацию
public class SynchronizerRequest extends AbstractMessage{

    private List<FileInfo> files;

    public SynchronizerRequest() {
    }

    public SynchronizerRequest(List<FileInfo> files) {
        this.files = files;
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }
}

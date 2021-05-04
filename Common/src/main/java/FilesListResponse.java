import java.io.File;
import java.util.Collection;
import java.util.List;

public class FilesListResponse extends AbstractMessage{

//    private List<File> files;
//
//    FilesListResponse(){}
//
//    public FilesListResponse(List<File> files) {
//        this.files = files;
//    }
//
//    public List<File> getFiles(){
//        return files;
//    }


    public FilesListResponse() {
    }

    private List<FileInfo> files;

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

import java.nio.file.Path;

public class DeleteRequest extends AbstractMessage{

    private Path delPath;

    public DeleteRequest() {
    }

    public DeleteRequest(Path delPath) {
        this.delPath = delPath;
    }

    public Path getDelPath() {
        return delPath;
    }

    public void setDelPath(Path delPath) {
        this.delPath = delPath;
    }
}

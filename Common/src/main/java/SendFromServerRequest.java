import java.nio.file.Path;

public class SendFromServerRequest extends AbstractMessage{

    public Path path;

    public SendFromServerRequest() {
    }

    public SendFromServerRequest(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}

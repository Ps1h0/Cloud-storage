public class SendFromServerRequest extends AbstractMessage{

    public String path;

    public SendFromServerRequest() {
    }

    public SendFromServerRequest(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

package backup.bridge;

public class FileEntry {
    private final String path;
    private final byte[] data;

    public FileEntry(String path, byte[] data) {
        this.path = path;
        this.data = data;
    }

    public String getPath() { return path; }
    public byte[] getData() { return data; }
}
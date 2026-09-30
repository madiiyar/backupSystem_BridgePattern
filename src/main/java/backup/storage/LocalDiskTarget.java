package backup.storage;

import backup.storage.exceptions.StorageException;
import backup.storage.exceptions.StorageIOException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalDiskTarget implements StorageTarget {

    private final Path baseDir;

    public LocalDiskTarget(Path baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public void upload(String path, byte[] data) throws StorageException {
        try {
            Path target = baseDir.resolve(path);
            Files.createDirectories(target.getParent());
            Files.write(target, data);
        } catch (IOException e) {
            throw new StorageIOException("Failed to write " + path, e);
        }
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(baseDir.resolve(path));
    }

    @Override
    public void delete(String path) throws StorageException {
        try {
            Files.deleteIfExists(baseDir.resolve(path));
        } catch (IOException e) {
            throw new StorageIOException("Failed to delete " + path, e);
        }
    }
}
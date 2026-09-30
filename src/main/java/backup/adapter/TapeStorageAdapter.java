package backup.adapter;

import backup.legacy.TapeArchiveDriver;
import backup.storage.StorageTarget;
import backup.storage.exceptions.StorageException;
import backup.storage.exceptions.StorageFullException;
import backup.storage.exceptions.StorageIOException;
import backup.storage.exceptions.StorageUnavailableException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TapeStorageAdapter implements StorageTarget {

    private final TapeArchiveDriver driver;   // composition — the adaptee

    public TapeStorageAdapter(TapeArchiveDriver driver) {
        this.driver = driver;
    }

    @Override
    public void upload(String path, byte[] data) throws StorageException {
        Path tempFile;
        try {
            tempFile = Files.createTempFile("tape-upload-", ".tmp");
            Files.write(tempFile, data);
        } catch (IOException e) {
            throw new StorageIOException("Failed to stage temp file for " + path, e);
        }
        try {
            int code = driver.put(path, tempFile.toString());
            translate(code, path);
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) { /* best-effort cleanup */ }
        }
    }

    @Override
    public boolean exists(String path) {
        return driver.stat(path) >= 0;   // synthesized, no direct legacy equivalent
    }

    @Override
    public void delete(String path) throws StorageException {
        int code = driver.erase(path);
        translate(code, path);
    }

    private void translate(int code, String path) throws StorageException {
        switch (code) {
            case TapeArchiveDriver.OK:
                return;
            case TapeArchiveDriver.ERR_NO_TAPE_MOUNTED:
                throw new StorageUnavailableException("No tape mounted for " + path);
            case TapeArchiveDriver.ERR_QUOTA_EXCEEDED:
                throw new StorageFullException("Tape quota exceeded for " + path);
            case TapeArchiveDriver.ERR_IO_FAILURE:
                throw new StorageIOException("Tape I/O failure for " + path);
            default:
                throw new StorageIOException("Unknown tape driver error code " + code + " for " + path);
        }
    }
}
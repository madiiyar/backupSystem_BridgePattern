package backup.storage;

import backup.storage.exceptions.StorageException;

public interface StorageTarget {
    void upload(String path, byte[] data) throws StorageException;
    boolean exists(String path) throws StorageException;
    void delete(String path) throws StorageException;
}
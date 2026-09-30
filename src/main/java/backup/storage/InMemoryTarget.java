package backup.storage;

import backup.storage.exceptions.StorageException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTarget implements StorageTarget {

    private final Map<String, byte[]> store = new ConcurrentHashMap<>();

    @Override
    public void upload(String path, byte[] data) throws StorageException {
        if (path == null || path.isBlank()) {
            throw new StorageException("Path must not be blank");
        }
        store.put(path, data);
    }

    @Override
    public boolean exists(String path) {
        return store.containsKey(path);
    }

    @Override
    public void delete(String path) {
        store.remove(path);
    }

    public byte[] read(String path) {   // test helper, not part of the interface
        return store.get(path);
    }
}
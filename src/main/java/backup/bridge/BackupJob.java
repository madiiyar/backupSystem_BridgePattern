package backup.bridge;

import backup.storage.StorageTarget;
import backup.storage.exceptions.StorageException;
import java.util.List;

public abstract class BackupJob {

    protected final StorageTarget target;   // ← the "bridge" — composition, not inheritance

    protected BackupJob(StorageTarget target) {
        this.target = target;
    }

    public abstract int run(List<FileEntry> files) throws StorageException;
}
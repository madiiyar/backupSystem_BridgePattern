package backup.app;

import backup.bridge.BackupJob;
import backup.bridge.FileEntry;
import backup.bridge.FullBackup;
import backup.bridge.IncrementalBackup;
import backup.storage.StorageTarget;
import backup.storage.exceptions.StorageException;

import java.util.List;

public class BackupService {

    private final StorageTargetResolver resolver;

    public BackupService(StorageTargetResolver resolver) {
        this.resolver = resolver;
    }

    public int backup(String destinationUri, List<FileEntry> files, BackupType type) throws StorageException {
        StorageTarget target = resolver.resolve(destinationUri);
        BackupJob job = switch (type) {
            case FULL -> new FullBackup(target);
            case INCREMENTAL -> new IncrementalBackup(target);
        };
        return job.run(files);
    }
}
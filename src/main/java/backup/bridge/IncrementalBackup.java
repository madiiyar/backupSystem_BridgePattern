package backup.bridge;

import backup.storage.StorageTarget;
import backup.storage.exceptions.StorageException;
import java.util.List;

public class IncrementalBackup extends BackupJob {

    public IncrementalBackup(StorageTarget target) {
        super(target);
    }

    @Override
    public int run(List<FileEntry> files) throws StorageException {
        int count = 0;
        for (FileEntry file : files) {
            if (!target.exists(file.getPath())) {
                target.upload(file.getPath(), file.getData());
                count++;
            }
        }
        return count;
    }
}
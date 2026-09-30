package backup.app;

import backup.bridge.FileEntry;
import backup.storage.exceptions.StorageException;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Main {
    public static void main(String[] args) throws StorageException {
        BackupService service = new BackupService(new StorageTargetResolver());

        List<FileEntry> files = List.of(
                new FileEntry("notes.txt", "hello world".getBytes(StandardCharsets.UTF_8)),
                new FileEntry("report.csv", "a,b,c\n1,2,3".getBytes(StandardCharsets.UTF_8))
        );

        int memCount = service.backup("mem://scratch", files, BackupType.FULL);
        System.out.println("Backed up to memory: " + memCount + " files");

        int tapeCount = service.backup("tape://vault-01", files, BackupType.FULL);
        System.out.println("Backed up to tape: " + tapeCount + " files");
    }
}
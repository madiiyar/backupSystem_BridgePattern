package backup.bridge;

import backup.storage.StorageTarget;
import backup.storage.exceptions.StorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FullBackupTest {

    @Mock
    StorageTarget target;

    @Test
    void uploadsEveryFileRegardlessOfExistence() throws StorageException {
        List<FileEntry> files = List.of(
                new FileEntry("a.txt", "A".getBytes()),
                new FileEntry("b.txt", "B".getBytes())
        );

        FullBackup job = new FullBackup(target);
        int count = job.run(files);

        assertEquals(2, count);
        verify(target).upload("a.txt", "A".getBytes());
        verify(target).upload("b.txt", "B".getBytes());
    }
}
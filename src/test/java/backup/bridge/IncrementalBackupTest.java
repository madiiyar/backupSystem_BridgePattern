package backup.bridge;

import backup.storage.StorageTarget;
import backup.storage.exceptions.StorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncrementalBackupTest {

    @Mock
    StorageTarget target;

    @Test
    void skipsFilesThatAlreadyExist() throws StorageException {
        FileEntry existing = new FileEntry("old.txt", "old".getBytes());
        FileEntry fresh = new FileEntry("new.txt", "new".getBytes());

        when(target.exists("old.txt")).thenReturn(true);
        when(target.exists("new.txt")).thenReturn(false);

        IncrementalBackup job = new IncrementalBackup(target);
        int count = job.run(List.of(existing, fresh));

        assertEquals(1, count);
        verify(target, never()).upload("old.txt", existing.getData());
        verify(target).upload("new.txt", fresh.getData());
    }
}
package backup.adapter;

import backup.legacy.TapeArchiveDriver;
import backup.storage.exceptions.StorageException;
import backup.storage.exceptions.StorageFullException;
import backup.storage.exceptions.StorageIOException;
import backup.storage.exceptions.StorageUnavailableException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TapeStorageAdapterTest {

    @Test
    void uploadThenExistsThenReadBackSucceeds() throws StorageException {
        TapeArchiveDriver driver = new TapeArchiveDriver(1_000_000);
        TapeStorageAdapter adapter = new TapeStorageAdapter(driver);

        byte[] data = "hello tape".getBytes();
        adapter.upload("greeting.txt", data);

        assertTrue(adapter.exists("greeting.txt"));
        assertArrayEquals(data, driver.readBack("greeting.txt"));
    }

    @Test
    void existsIsFalseForUnknownPath() {
        TapeStorageAdapter adapter = new TapeStorageAdapter(new TapeArchiveDriver(1_000_000));
        assertFalse(adapter.exists("nothing-here.txt"));
    }

    @Test
    void noTapeMountedBecomesStorageUnavailableException() {
        TapeArchiveDriver driver = new TapeArchiveDriver(1_000_000);
        driver.setMounted(false);
        TapeStorageAdapter adapter = new TapeStorageAdapter(driver);

        assertThrows(StorageUnavailableException.class,
                () -> adapter.upload("a.txt", "data".getBytes()));
    }

    @Test
    void quotaExceededBecomesStorageFullException() {
        TapeArchiveDriver driver = new TapeArchiveDriver(2);   // tiny capacity
        TapeStorageAdapter adapter = new TapeStorageAdapter(driver);

        assertThrows(StorageFullException.class,
                () -> adapter.upload("big.txt", "way too much data".getBytes()));
    }

    @Test
    void unknownDriverCodeBecomesStorageIOException() {
        TapeArchiveDriver weird = new TapeArchiveDriver(1_000_000) {
            @Override
            public int put(String tapeLabel, String localFilePath) {
                return -99;   // a code the adapter has never seen
            }
        };
        TapeStorageAdapter adapter = new TapeStorageAdapter(weird);

        assertThrows(StorageIOException.class,
                () -> adapter.upload("a.txt", "data".getBytes()));
    }
}
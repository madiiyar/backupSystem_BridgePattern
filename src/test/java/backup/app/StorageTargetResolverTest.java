package backup.app;

import backup.adapter.TapeStorageAdapter;
import backup.storage.InMemoryTarget;
import backup.storage.LocalDiskTarget;
import backup.storage.StorageTarget;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageTargetResolverTest {

    private final StorageTargetResolver resolver = new StorageTargetResolver();

    @Test
    void fileSchemeResolvesToLocalDiskTarget() {
        assertInstanceOf(LocalDiskTarget.class, resolver.resolve("file:///tmp/backups"));
    }

    @Test
    void memSchemeResolvesToInMemoryTarget() {
        assertInstanceOf(InMemoryTarget.class, resolver.resolve("mem://scratch"));
    }

    @Test
    void tapeSchemeResolvesToAdaptedLegacyDriver() {
        assertInstanceOf(TapeStorageAdapter.class, resolver.resolve("tape://vault-01"));
    }

    @Test
    void unknownSchemeThrows() {
        assertThrows(IllegalArgumentException.class, () -> resolver.resolve("ftp://old-server"));
    }
}
package backup.app;

import backup.adapter.TapeStorageAdapter;
import backup.legacy.TapeArchiveDriver;
import backup.storage.InMemoryTarget;
import backup.storage.LocalDiskTarget;
import backup.storage.StorageTarget;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StorageTargetResolver {

    private final Map<String, Function<URI, StorageTarget>> factories = new HashMap<>();

    public StorageTargetResolver() {
        factories.put("file", uri ->
                new LocalDiskTarget(Path.of(uri.getSchemeSpecificPart().replaceFirst("^//", ""))));
        factories.put("mem", uri -> new InMemoryTarget());
        factories.put("tape", uri ->
                new TapeStorageAdapter(new TapeArchiveDriver(1_000_000_000L)));
    }

    public StorageTarget resolve(String uriString) {
        URI uri = URI.create(uriString);
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("URI must include a scheme (file/mem/tape): " + uriString);
        }
        Function<URI, StorageTarget> factory = factories.get(scheme);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown storage scheme: " + scheme);
        }
        return factory.apply(uri);
    }
}
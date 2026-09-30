package backup.legacy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class TapeArchiveDriver {

    public static final int OK = 0;
    public static final int ERR_NO_TAPE_MOUNTED = -1;
    public static final int ERR_QUOTA_EXCEEDED = -3;
    public static final int ERR_IO_FAILURE = -7;

    private final Map<String, byte[]> tape = new HashMap<>();
    private final long capacityBytes;
    private long usedBytes = 0;
    private boolean mounted = true;

    public TapeArchiveDriver(long capacityBytes) {
        this.capacityBytes = capacityBytes;
    }

    public void setMounted(boolean mounted) {   // test hook only
        this.mounted = mounted;
    }

    public int put(String tapeLabel, String localFilePath) {
        if (!mounted) return ERR_NO_TAPE_MOUNTED;
        try {
            byte[] data = Files.readAllBytes(new File(localFilePath).toPath());
            if (usedBytes + data.length > capacityBytes) return ERR_QUOTA_EXCEEDED;
            tape.put(tapeLabel, data);
            usedBytes += data.length;
            return OK;
        } catch (IOException e) {
            return ERR_IO_FAILURE;
        }
    }

    public long stat(String tapeLabel) {
        byte[] data = tape.get(tapeLabel);
        return data == null ? -1 : data.length;
    }

    public int erase(String tapeLabel) {
        if (!mounted) return ERR_NO_TAPE_MOUNTED;
        byte[] removed = tape.remove(tapeLabel);
        if (removed != null) usedBytes -= removed.length;
        return OK;
    }

    public byte[] readBack(String tapeLabel) {   // test helper only
        return tape.get(tapeLabel);
    }
}
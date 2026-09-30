// StorageIOException.java
package backup.storage.exceptions;

public class StorageIOException extends StorageException {
    public StorageIOException(String message) { super(message); }
    public StorageIOException(String message, Throwable cause) { super(message, cause); }
}
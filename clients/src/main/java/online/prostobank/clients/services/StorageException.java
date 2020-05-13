package online.prostobank.clients.services;

/**
 * Ошибки при взаимодействии с хранилищем файлов
 */
public class StorageException extends Exception {
	public StorageException(String message) {
		super(message);
	}

	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}
}

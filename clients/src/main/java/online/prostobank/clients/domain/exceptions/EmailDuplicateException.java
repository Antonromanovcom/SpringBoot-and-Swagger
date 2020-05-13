package online.prostobank.clients.domain.exceptions;

public class EmailDuplicateException extends Exception {
	public EmailDuplicateException(String message) {
		super(message);
	}
}

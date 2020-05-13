package online.prostobank.clients.domain.exceptions;

public class KonturFailException extends Exception {
	private String userMessage;

	public KonturFailException(String message, String userMessage) {
		super(message);
		this.userMessage = userMessage;
	}

	public String getUserMessage() {
		return userMessage;
	}
}

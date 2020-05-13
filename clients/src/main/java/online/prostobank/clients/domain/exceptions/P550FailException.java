package online.prostobank.clients.domain.exceptions;

public class P550FailException extends Exception {
	private String userMessage;

	public P550FailException(String message, String userMessage) {
		super(message);
		this.userMessage = userMessage;
	}

	public String getUserMessage() {
		return userMessage;
	}
}

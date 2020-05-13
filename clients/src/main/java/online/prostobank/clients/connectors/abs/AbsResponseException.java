package online.prostobank.clients.connectors.abs;

public class AbsResponseException extends Exception {
	private static final String DUPLICATE_CODE = "H0206";
	private String errorCode;
	public AbsResponseException(String message) {
		super(message);
	}

	public AbsResponseException(String message, Throwable cause) {
		super(message, cause);
	}

	public AbsResponseException(String message, String errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public boolean isDuplicateError() {
		return errorCode != null && errorCode.trim().equals(DUPLICATE_CODE);
	}
}

package online.prostobank.clients.domain.exceptions;

public class RiskyOkvedException extends Exception { // на самом деле не исключение вообще, но развитие событий для таких иное

	public RiskyOkvedException(String message) {
		super(message);
	}
}

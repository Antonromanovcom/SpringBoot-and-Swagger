package online.prostobank.clients.connectors.api;

import lombok.Getter;
import lombok.Setter;
import online.prostobank.clients.connectors.abs.AbsResponseException;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.AccountValue;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Работа с АБС
 */
public interface AbsService {
	/**
	 * Проверка на наличие в 550х списках
	 *
	 * @param taxNumber - ИНН или ОГРН
	 * @return - {@link CheckResult}
	 */
	@NotNull(message = "Параметр не задан") CheckResult doCheck(@NotEmpty(message = "Парамерт не может быть пустым") String taxNumber);

	AccountValue makeReservation(AccountApplication app) throws AbsResponseException;

	@Getter
	@Setter
	class CheckResult {
		private String[] errorReasons;
		private Boolean isSuspicious;
	}
}

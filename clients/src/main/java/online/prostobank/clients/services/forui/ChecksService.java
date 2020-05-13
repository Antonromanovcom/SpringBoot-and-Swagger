package online.prostobank.clients.services.forui;

import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.enums.FailReason;
import org.springframework.data.util.Pair;

import java.util.Optional;

public interface ChecksService {
	/**
	 * Проверка в контуре
	 *
	 * @param app - заявка
	 * @return результат операции и сохранения
	 */
	Pair<Optional<Exception>, AccountApplication> kontur(AccountApplication app);

	/**
	 * Проверка на аресты
	 *
	 * @param app - заявка
	 * @return результат операции и сохранения
	 */
	Pair<Boolean, AccountApplication> arrestsFns(AccountApplication app);

	/**
	 * Проверка 550-п
	 *
	 * @param app - заявка
	 * @return результат операции и сохранения
	 */
	Pair<Optional<Exception>, AccountApplication> recheckP550(AccountApplication app);

	/**
	 * Проверка паспортных данных
	 *
	 * @param app - заявка
	 * @return результат операции и сохранения
	 */
	Pair<Boolean, AccountApplication> checkPassport(AccountApplication app);

	/**
	 * Резервирование счета
	 *
	 * @param app - заявка
	 * @return результат операции и сохранения
	 */
	Pair<Optional<FailReason>, AccountApplication> reserve(AccountApplication app);
}

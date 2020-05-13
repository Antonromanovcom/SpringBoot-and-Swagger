package online.prostobank.clients.domain.repository;

import online.prostobank.clients.domain.AccountApplication;
import org.springframework.data.util.Pair;

import javax.annotation.Nonnull;

public interface AccountApplicationRepositoryWrapper {
	/**
	 * Сохранение зявки
	 *
	 * @param application - заявка
	 * @param toEmarsys   - надо ли отправлять изменения в емарсис
	 * @return результат сохранения
	 */
	Pair<Boolean, AccountApplication> saveAccountApplication(@Nonnull AccountApplication application, boolean toEmarsys);

	/**
	 * Сохранение зявки
	 *
	 * @param application - заявка
	 * @return результат сохранения
	 */
	Pair<Boolean, AccountApplication> saveAccountApplication(@Nonnull AccountApplication application);
}

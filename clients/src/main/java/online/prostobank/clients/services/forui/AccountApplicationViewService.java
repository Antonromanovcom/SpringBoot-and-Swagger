package online.prostobank.clients.services.forui;

import online.prostobank.clients.domain.AccountApplication;
import org.springframework.data.util.Pair;

import javax.annotation.Nonnull;

public interface AccountApplicationViewService {
	/**
	 * Отправка СМС-напоминания
	 *
	 * @param application - заявка
	 * @return последнее время отправки и результат сохранения
	 */
	Pair<String, AccountApplication> smsRemind(AccountApplication application);

	/**
	 * Сохранение зявки
	 *
	 * @param application - заявка
	 * @return результат сохранения
	 */
	Pair<Boolean, AccountApplication> saveAccountApplication(@Nonnull AccountApplication application);

	/**
	 * Экспорт в PDF
	 *
	 * @param application - заявка
	 * @return pdf контент
	 */
	byte[] pdfListener(AccountApplication application);

	/**
	 * Начать работу над заявкой
	 *
	 * @param application - заявка
	 * @param name        - имя юзера
	 * @return результат сохранения
	 */
	Pair<Boolean, AccountApplication> startWork(AccountApplication application, String name);

	/**
	 * Переназначить на пользователя
	 *
	 * @param application - заявка
	 * @param username    - логин пользователя
	 * @return результат сохранения
	 */
	Pair<Boolean, AccountApplication> setAssignedTo(AccountApplication application, @Nonnull String username);

	/**
	 * Сохранить комментарий
	 *
	 * @param application - заявка
	 * @param text        - комментарий
	 * @param username    - логин пользователя
	 * @return результат сохранения
	 */
	Pair<Boolean, AccountApplication> saveComment(AccountApplication application, String text, String username);

	/**
	 * Сбросить код подтверждения
	 *
	 * @param application - заявка
	 * @return результат сохранения
	 */
	Pair<Boolean, AccountApplication> resetConfirmationCode(AccountApplication application);

	/**
	 * Отправка смс с проверками
	 *
	 * @param application - заявка
	 * @return результат отправки
	 */
	Pair<Boolean, AccountApplication> decideAndSendSms(AccountApplication application);

	/**
	 * Смс подтверждена
	 *
	 * @param application - заявка
	 * @param code        - код из смс
	 * @return результат сохранения
	 */
	Pair<Boolean, AccountApplication> smsConfirmationAddHistory(AccountApplication application, String code);
}

package online.prostobank.clients.domain.events;

import online.prostobank.clients.domain.AccountApplication;

/**
 * Событие о том, что пользователю надо бы выслать полную информацию о доступе в его кабинет по заявке
 */
public class InformClientEvent extends AccountApplicationEvent {
	public InformClientEvent(AccountApplication accApp) {
		super(accApp);
	}
}

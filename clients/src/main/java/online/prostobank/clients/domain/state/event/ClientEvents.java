package online.prostobank.clients.domain.state.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * События клиента для изменения статусов
 */
@Getter
@RequiredArgsConstructor
public enum ClientEvents {

	CE_SMS("Смс с кодом подтверждена"),
	CLIENT_DECLINE("Клиент отказался"),
	TO_NEW_CLIENT(""),
	TO_WAIT_FOR_DOCS(""),
	CONFIRMED(""),
	CLIENT_NOT_RESPONDING("Клиент не отвечает"),
	CHECKS_DONE(""),
	TRY_RESERVE(""),
	CHECKS(""),
	NEED_DOCS(""),
	RESERVE(""),
	DOCS_ADDED(""),
	ACCOUNT_CLOSE(""),
	ACCOUNT_OPEN(""),
	MAKE_COLD(""),
	AUTO_DECLINE(""),
	AT_LEAST_ONE_DOCUMENTS_LOADED(""),
	;

	private final String ruName;
}

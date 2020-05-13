package online.prostobank.clients.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Event {
	CREATE(EventName.CREATE_NAME),
	CHANGE_DATA(EventName.CHANGE_DATA_NAME),
	DOCS_IS_DONE(EventName.DOCS_IS_DONE_NAME),
	EMARSYS_CONTACT(EventName.EMAIL_NAME),
	EMARSYS_TRIGGER(EventName.TRIGGER_NAME),
	SYSTEM_EMAIL(EventName.SYSTEM_EMAIL_NAME),
	SMS(EventName.SMS_NAME),
	KEYCLOAK(EventName.KEYCLOAK_NAME),
	DBO(EventName.DBO_NAME),
	;

	private final String name;
}

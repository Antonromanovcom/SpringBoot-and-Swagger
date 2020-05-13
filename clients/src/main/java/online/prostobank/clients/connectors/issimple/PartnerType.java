package online.prostobank.clients.connectors.issimple;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartnerType {
	PROSTO("Просто|Банк"),
	ELBA("Эльба|Банк"),
	AVUAR("Авуар"),
	;

	private final String description;
}

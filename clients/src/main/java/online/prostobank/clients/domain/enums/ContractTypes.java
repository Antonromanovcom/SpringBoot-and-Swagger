package online.prostobank.clients.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import online.prostobank.clients.domain.Contracts;

import java.util.function.BiConsumer;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum ContractTypes {
	SUPPLY("Договор поставки", Contracts::setProduce, Contracts::isProduce),
	SALE("Договор купли-продажи", Contracts::setMarket, Contracts::isMarket),
	LOAN("Договор займа", Contracts::setLoan, Contracts::isLoan),
	LEASE("Договор аренды", Contracts::setLend, Contracts::isLend),
	WORK("Договор подряда", Contracts::setSubcontract, Contracts::isSubcontract),
	SECURITIES("Договор, связанный с ценными бумагами", Contracts::setActives, Contracts::isActives),
	SERVICE("Договор услуг", Contracts::setService, Contracts::isService),
	LICENCE("Лицензионный договор", Contracts::setLicense, Contracts::isLicense),
	AGENT("Агентский договор", Contracts::setAgent, Contracts::isAgent),
	OTHER("Другое", Contracts::setOther, Contracts::isOther),
	;

	private final String ruName;
	private final BiConsumer<Contracts, Boolean> setter;
	private final Function<Contracts, Boolean> getter;
}

package online.prostobank.clients.domain.enums;

public enum ClientType {
	SP("ИП")
	, LLC("ООО")
	, UNKNOWN("Не определен")
	;

	ClientType(String russianName) {
		this.russianName = russianName;
	}

	String russianName;
}

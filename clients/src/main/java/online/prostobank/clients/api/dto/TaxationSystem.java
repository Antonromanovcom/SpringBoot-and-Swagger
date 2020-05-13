package online.prostobank.clients.api.dto;

/**
 * Система налогообложения (нужно для передачи в МД)
 */
public enum TaxationSystem {

	IP( 1, "ИП"),
	OOO(2, "ООО");

	private Integer code; // код, который передаем в МД
	private String taxationSystemName; // наименование системы налогообложения

	TaxationSystem(Integer code, String taxationSystemName) {
		this.code = code;
		this.taxationSystemName = taxationSystemName;
	}
}

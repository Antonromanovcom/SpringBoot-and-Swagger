package online.prostobank.clients.api.dto;

import online.prostobank.clients.domain.enums.ClientType;

/**
 * Запрос внешнего колл-центра на создание заявки
 */
public class CallCenterAccountApplicationDTO {
	public String taxNumber;
	public String fio;
	public String phone;
	public String address;
	public String email;
	public String city;
	public String comment;
	public ClientType legalType; // Sole Proprietorship (ИП)
	public String companyName;

}

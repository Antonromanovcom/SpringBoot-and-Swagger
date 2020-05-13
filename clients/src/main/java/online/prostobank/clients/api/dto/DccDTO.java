package online.prostobank.clients.api.dto;

import lombok.Getter;

/**
 * json с параметрами для добавления заявки в CRM
 */
@Getter
public class DccDTO {
	private String taxNumber; // обязательный
	private String phone; // обязательный
	private String operator; // обязательный

	private String fio;
	private String address;
	private String email;
	private String comment;
}
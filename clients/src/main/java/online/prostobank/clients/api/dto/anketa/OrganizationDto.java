package online.prostobank.clients.api.dto.anketa;

import lombok.Data;

@Data
public class OrganizationDto {
	private String orgName;
	private String inn;
	private String ogrn;
	private String kpp;
	private String regDate;
	private String regPlace;
	private String clientName;
	private String type;
	private String error;
}

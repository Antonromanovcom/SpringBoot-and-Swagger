package online.prostobank.clients.services.dbo.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DboResponseCreateUserDto {
	private String id;
	private String surname;
	private String name;
	private String patronymic;
	private String email;
	private String phone;
	private LegEntityDto legEntity;
}

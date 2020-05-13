package online.prostobank.clients.api.dto.client;

import lombok.Data;

@Data
public class ClientCardCreateDTO {
	private String phone;
	private String inn;
	private String email;
	private Long city;
	private String comment;
}

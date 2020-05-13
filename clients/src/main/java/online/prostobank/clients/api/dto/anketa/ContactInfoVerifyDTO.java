package online.prostobank.clients.api.dto.anketa;

import lombok.Data;

@Data
public class ContactInfoVerifyDTO {
    private String city;
    private String partner;
    private String phone;
    private String userId;
	private UtmDTO utm;
}
